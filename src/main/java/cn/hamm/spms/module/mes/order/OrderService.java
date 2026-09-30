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
     * {@code ORDER_MANUAL_FINISH} 配置的说明写着「允许在任何情况下手动完成订单」，
     * 因此订单的 {@code PRODUCING} / {@code PAUSED} 状态都允许被标记完成，
     * 而基类的守卫只放行「已审核」与「明细已完成」两种状态。
     * <p>
     * 这里返回配置值而不是硬编码 {@code true}：管理员把这个开关关掉后，
     * 订单就重新受基类守卫约束，语义与配置描述一致。
     * <p>
     * 注意本钩子只放开<b>基类</b>的守卫，真正的状态校验在
     * {@link #setOrderFinishedManually(long)} 里（只允许准备中/生产中/暂停中）。
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
     * {@code setBillDetailsAllFinished} 是 {@code final}，重复调用会重复执行
     * {@code afterAllBillDetailFinished} 生成入库单，导致库存凭空翻倍；
     * 这里补上状态守卫，同时起到幂等保护的作用。
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
     * 整段收进一个事务，并使用 {@code getForUpdate} 带行锁读取订单：
     * 原实现读-改-写无锁无事务，两个操作工并发报工会互相覆盖，
     * 造成订单完成数量永远追不上明细实际报工量。
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
            // 悲观锁读取，避免并发报工互相覆盖
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

        if (OrderType.PLAN.equalsKey(orderBill.getType())) {
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