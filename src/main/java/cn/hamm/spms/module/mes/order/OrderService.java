package cn.hamm.spms.module.mes.order;

import cn.hamm.airpower.core.DictionaryUtil;
import cn.hamm.airpower.core.NumberUtil;
import cn.hamm.airpower.core.interfaces.IDictionary;
import cn.hamm.spms.base.bill.AbstractBaseBillService;
import cn.hamm.spms.module.mes.MesServices;
import cn.hamm.spms.module.mes.order.detail.OrderDetailEntity;
import cn.hamm.spms.module.mes.order.detail.OrderDetailRepository;
import cn.hamm.spms.module.mes.order.detail.OrderDetailService;
import cn.hamm.spms.module.mes.order.enums.OrderStatus;
import cn.hamm.spms.module.mes.order.enums.OrderType;
import cn.hamm.spms.module.system.SystemServices;
import cn.hamm.spms.module.system.config.ConfigEntity;
import cn.hamm.spms.module.system.config.ConfigService;
import cn.hamm.spms.module.system.config.enums.ConfigFlag;
import cn.hamm.spms.module.wms.WmsServices;
import cn.hamm.spms.module.wms.input.InputEntity;
import cn.hamm.spms.module.wms.input.detail.InputDetailEntity;
import cn.hamm.spms.module.wms.input.enums.InputType;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static cn.hamm.airpower.exception.Errors.FORBIDDEN;
import static cn.hamm.airpower.exception.Errors.PARAM_INVALID;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Slf4j
@Service
public class OrderService extends AbstractBaseBillService<OrderEntity, OrderRepository, OrderDetailEntity, OrderDetailService, OrderDetailRepository> {
    @Override
    public IDictionary getAuditedStatus() {
        return OrderStatus.PREPARE;
    }

    @Override
    public IDictionary getAuditingStatus() {
        return OrderStatus.AUDITING;
    }

    @Override
    public IDictionary getRejectedStatus() {
        return OrderStatus.REJECTED;
    }

    @Override
    public IDictionary getBillDetailsFinishStatus() {
        return OrderStatus.INPUTTING;
    }

    @Override
    public IDictionary getFinishedStatus() {
        return OrderStatus.DONE;
    }

    /**
     * 订单豁免基类的完成状态守卫
     * <p>
     * 返回配置值而非硬编码 true：管理员关掉开关后，订单重新受基类守卫约束。
     * 本钩子只放开基类守卫，真正的状态校验在 {@link #setOrderFinishedManually(long)}。
     * </p>
     *
     * @return true 表示跳过基类状态守卫
     */
    @Override
    protected boolean isForceFinishAllowed() {
        return SystemServices.getConfigService().get(ConfigFlag.ORDER_MANUAL_FINISH).booleanConfig();
    }

    /**
     * <h1>手动标记订单生产完成</h1>
     * <p>
     * 状态守卫同时起幂等保护：重复调用不会重复生成入库单、不会让库存翻倍。
     * </p>
     *
     * @param orderId 订单 ID
     */
    public void setOrderFinishedManually(long orderId) {
        OrderEntity exist = get(orderId);
        boolean canFinish = List.of(
                OrderStatus.PREPARE.getKey(),
                OrderStatus.PRODUCING.getKey(),
                OrderStatus.PAUSED.getKey()
        ).contains(exist.getStatus());
        FORBIDDEN.when(!canFinish, String.format(
                "订单当前状态为「%s」，不允许标记生产完成",
                DictionaryUtil.getDictionary(OrderStatus.class, exist.getStatus()).getLabel()));
        setBillDetailsAllFinished(orderId);
    }

    /**
     * 添加订单明细
     * <p>
     * 收进一个事务并用 {@code getForUpdate} 带行锁读取订单：
     * 否则两个操作工并发报工会互相覆盖，订单完成数量追不上明细实际报工量。
     * </p>
     *
     * @param orderDetail 订单明细
     */
    public void addOrderDetail(@NotNull OrderDetailEntity orderDetail) {
        ConfigService configService = SystemServices.getConfigService();
        ConfigEntity config = configService.get(ConfigFlag.ORDER_ENABLE_SUBMIT_WORK);
        FORBIDDEN.when(!config.booleanConfig(), "未开启订单报工模式");
        PARAM_INVALID.whenNull(orderDetail.getBillId(), "订单ID不能为空");

        transactionHelper.run(() -> {
            OrderEntity order = getForUpdate(orderDetail.getBillId());

            boolean canReport = List.of(
                    OrderStatus.PREPARE.getKey(),
                    OrderStatus.PRODUCING.getKey(),
                    OrderStatus.PAUSED.getKey()
            ).contains(order.getStatus());
            FORBIDDEN.when(!canReport, String.format("订单当前状态为「%s」，无法报工",
                    DictionaryUtil.getDictionary(OrderStatus.class, order.getStatus()).getLabel()));

            double reportQuantity = Objects.requireNonNullElse(orderDetail.getQuantity(), 0D);
            double ngQuantity = Objects.requireNonNullElse(orderDetail.getNgQuantity(), 0D);
            PARAM_INVALID.when(reportQuantity <= 0D, "报工数量必须大于 0");
            double finishQuantity = Objects.requireNonNullElse(order.getFinishQuantity(), 0D);
            double remainQuantity = NumberUtil.subtract(order.getQuantity(), finishQuantity);
            PARAM_INVALID.when(reportQuantity > remainQuantity, String.format(
                    "本次报工 %s 超过订单剩余待完成数量 %s", reportQuantity, remainQuantity));

            // 更新明细数量和状态
            orderDetail.setQuantity(reportQuantity)
                    .setFinishQuantity(reportQuantity)
                    .setNgQuantity(ngQuantity)
                    .setIsFinished(true);
            OrderDetailService orderDetailService = MesServices.getOrderDetailService();
            orderDetailService.add(orderDetail);

            // 更新订单数量
            List<OrderDetailEntity> details = orderDetailService.getAllByBillId(order.getId());
            double totalFinishQuantity = 0D;
            double tatalNgQuantity = 0D;
            for (OrderDetailEntity detail : details) {
                totalFinishQuantity = NumberUtil.add(totalFinishQuantity,
                        Objects.requireNonNullElse(detail.getFinishQuantity(), 0D));
                tatalNgQuantity = NumberUtil.add(tatalNgQuantity,
                        Objects.requireNonNullElse(detail.getNgQuantity(), 0D));
            }
            order.setFinishQuantity(totalFinishQuantity)
                    .setNgQuantity(tatalNgQuantity)
            ;
            updateToDatabase(order);

            ConfigEntity autoFinish = configService.get(ConfigFlag.ORDER_AUTO_FINISH);
            if (autoFinish.booleanConfig() && totalFinishQuantity >= order.getQuantity()) {
                setBillDetailsAllFinished(order.getId());
            }
        });
    }

    @Override
    protected void afterBillFinished(long billId) {
        updateToDatabase(getEntityInstance(billId)
                .setFinishTime(System.currentTimeMillis())
        );
    }

    @Override
    protected void afterAllBillDetailFinished(long billId) {
        OrderEntity orderBill = get(billId);
        if (orderBill.getFinishQuantity() == 0) {
            // 直接完成 无需入库
            orderBill.setStatus(OrderStatus.DONE.getKey())
                    .setFinishTime(System.currentTimeMillis());
            updateToDatabase(orderBill);
            return;
        }
        // 添加入库单
        addInputBill(orderBill);

        // 以 plan 是否存在为准，不看 type：两者本是同一件事的两个副本，
        // 历史数据里存在 type=计划订单 而 plan 为空，在此处空指针。
        if (Objects.isNull(orderBill.getPlan())) {
            return;
        }
        // 更新计划单
        MesServices.getPlanDetailService().updateDetailQuantity(
                orderBill.getPlan().getId(),
                orderBill.getFinishQuantity(),
                MesServices.getPlanService(),
                detail -> FORBIDDEN.whenNotEquals(
                        detail.getMaterial().getId(),
                        orderBill.getMaterial().getId(),
                        "物料信息不匹配")
        );
    }

    /**
     * 添加入库单
     *
     * @param order 订单
     */
    private void addInputBill(OrderEntity order) {
        InputEntity input = new InputEntity();
        input.setType(InputType.PRODUCTION.getKey());
        input.setOrder(order);
        InputEntity inputSaved = WmsServices.getInputService().addAndGet(input);
        WmsServices.getInputDetailService().add(new InputDetailEntity()
                .setQuantity(order.getFinishQuantity())
                .setBillId(inputSaved.getId())
                .setMaterial(order.getMaterial())
        );
    }

    @Override
    protected @NotNull OrderEntity beforeAdd(@NotNull OrderEntity order) {
        order.setDetails(new ArrayList<>());
        return syncTypeWithPlan(order);
    }

    @Override
    protected @NotNull OrderEntity beforeUpdate(@NotNull OrderEntity order) {
        return syncTypeWithPlan(order);
    }

    /**
     * 让订单类型与计划单保持一致
     * <p>
     * 「是不是计划订单」就是「有没有挂计划单」，不该由两个字段各存一份，
     * 否则会出现 type=计划订单、plan=null 的自相矛盾数据。
     * </p>
     *
     * @param order 订单
     * @return 处理后的订单
     */
    private @NotNull OrderEntity syncTypeWithPlan(@NotNull OrderEntity order) {
        boolean hasPlan = Objects.nonNull(order.getPlan());
        int expected = hasPlan ? OrderType.PLAN.getKey() : OrderType.OTHER.getKey();
        if (!Objects.equals(order.getType(), expected)) {
            log.info("订单 {} 的类型与计划单不一致，按 plan={} 纠正为「{}」",
                    order.getId(), hasPlan ? order.getPlan().getId() : null,
                    DictionaryUtil.getDictionary(OrderType.class, expected).getLabel());
            order.setType(expected);
        }
        return order;
    }

    @Override
    protected void afterBillAudited(long billId) {
        ConfigEntity config = SystemServices.getConfigService().get(ConfigFlag.ORDER_AUTO_START_AFTER_AUDIT);
        if (config.booleanConfig()) {
            start(billId);
        }
    }

    /**
     * 开始生产
     *
     * @param id 单据 ID
     */
    public final void start(long id) {
        OrderEntity order = get(id);
        OrderStatus[] canStartStatusList = {OrderStatus.PREPARE, OrderStatus.PAUSED};
        OrderStatus currentStatus = DictionaryUtil.getDictionary(OrderStatus.class, order.getStatus());
        FORBIDDEN.when(!Arrays.asList(canStartStatusList).contains(currentStatus), "该单据状态无法开始生产");
        updateToDatabase(getEntityInstance(id).setStatus(OrderStatus.PRODUCING.getKey()));
    }

    /**
     * 暂停生产
     *
     * @param id 单据 ID
     */
    public final void pause(long id) {
        OrderEntity order = get(id);
        OrderStatus[] canPauseStatusList = {OrderStatus.PRODUCING};
        OrderStatus currentStatus = DictionaryUtil.getDictionary(OrderStatus.class, order.getStatus());
        FORBIDDEN.when(!Arrays.asList(canPauseStatusList).contains(currentStatus), "该单据状态无法暂停生产");
        updateToDatabase(getEntityInstance(id).setStatus(OrderStatus.PAUSED.getKey()));
    }
}