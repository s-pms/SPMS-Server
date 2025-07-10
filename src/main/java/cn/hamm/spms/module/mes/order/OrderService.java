package cn.hamm.spms.module.mes.order;

import cn.hamm.airpower.dictionary.DictionaryUtil;
import cn.hamm.airpower.dictionary.IDictionary;
import cn.hamm.airpower.util.NumberUtil;
import cn.hamm.spms.base.bill.AbstractBaseBillService;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.mes.order.detail.OrderDetailEntity;
import cn.hamm.spms.module.mes.order.detail.OrderDetailRepository;
import cn.hamm.spms.module.mes.order.detail.OrderDetailService;
import cn.hamm.spms.module.system.config.ConfigEntity;
import cn.hamm.spms.module.system.config.ConfigFlag;
import cn.hamm.spms.module.system.config.ConfigService;
import cn.hamm.spms.module.wms.input.InputEntity;
import cn.hamm.spms.module.wms.input.InputService;
import cn.hamm.spms.module.wms.input.InputType;
import cn.hamm.spms.module.wms.input.detail.InputDetailEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cn.hamm.airpower.exception.ServiceError.FORBIDDEN;

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
     * 添加订单明细
     *
     * @param orderDetail 订单明细
     */
    public void addOrderDetail(@NotNull OrderDetailEntity orderDetail) {
        ConfigService configService = Services.getConfigService();
        ConfigEntity config = configService.get(ConfigFlag.ORDER_ENABLE_SUBMIT_WORK);
        FORBIDDEN.when(!config.booleanConfig(), "未开启订单报工模式");
        // 更新明细数量和状态
        orderDetail.setQuantity(orderDetail.getFinishQuantity()).setIsFinished(true);
        OrderDetailService orderDetailService = Services.getOrderDetailService();
        orderDetailService.add(orderDetail);

        // 更新订单数量
        OrderEntity order = get(orderDetail.getBillId());
        List<OrderDetailEntity> details = orderDetailService.getAllByBillId(order.getId());
        double finishQuantity = 0D;
        double ngQuantity = 0D;
        for (OrderDetailEntity detail : details) {
            finishQuantity = NumberUtil.add(finishQuantity, detail.getFinishQuantity());
            ngQuantity = NumberUtil.add(ngQuantity, detail.getNgQuantity());
        }
        order.setFinishQuantity(finishQuantity)
                .setNgQuantity(ngQuantity)
        ;
        update(order);

        config = configService.get(ConfigFlag.ORDER_AUTO_FINISH);
        if (config.booleanConfig() && order.getFinishQuantity() >= order.getQuantity()) {
            setBillDetailsAllFinished(order.getId());
        }
    }

    @Override
    protected void afterAllBillDetailFinished(long billId) {
        OrderEntity orderBill = get(billId);
        if (orderBill.getFinishQuantity() == 0) {
            // 直接完成 无需入库
            orderBill.setStatus(OrderStatus.DONE.getKey())
                    .setFinishTime(System.currentTimeMillis());
            update(orderBill);
            return;
        }

        if (OrderType.PLAN.equalsKey(orderBill.getType())) {
            // 更新计划单
            Services.getPlanDetailService().updateDetailQuantity(
                    orderBill.getPlan().getId(),
                    orderBill.getFinishQuantity(),
                    Services.getPlanService(),
                    detail -> FORBIDDEN.whenNotEquals(
                            detail.getMaterial().getId(),
                            orderBill.getMaterial().getId(),
                            "物料信息不匹配")
            );
        }
        // 添加入库单
        addInputBill(orderBill);
    }

    /**
     * 添加入库单
     *
     * @param order 订单
     */
    private void addInputBill(OrderEntity order) {
        InputService inputService = Services.getInputService();
        InputEntity input = new InputEntity();
        input.setType(InputType.PRODUCTION.getKey());
        input.setOrder(order);
        long inputId = inputService.add(input);

        List<InputDetailEntity> details = new ArrayList<>();
        details.add(new InputDetailEntity()
                .setQuantity(order.getFinishQuantity())
                .setBillId(inputId)
                .setMaterial(order.getMaterial())
        );
        input = inputService.get(inputId);
        input.setDetails(details);
        inputService.update(input);
    }

    @Override
    protected @NotNull OrderEntity beforeAdd(@NotNull OrderEntity order) {
        order.setDetails(new ArrayList<>());
        return order;
    }

    @Override
    protected void afterBillAudited(long id) {
        ConfigEntity config = Services.getConfigService().get(ConfigFlag.ORDER_AUTO_START_AFTER_AUDIT);
        if (config.booleanConfig()) {
            start(id);
        }
    }

    /**
     * 开始生产
     *
     * @param id 单据ID
     */
    public final void start(long id) {
        OrderEntity order = get(id);
        OrderStatus[] canStartStatusList = {OrderStatus.PREPARE, OrderStatus.PAUSED};
        OrderStatus currentStatus = DictionaryUtil.getDictionary(OrderStatus.class, order.getStatus());
        FORBIDDEN.when(!Arrays.asList(canStartStatusList).contains(currentStatus), "该单据状态无法开始生产");
        order.setStatus(OrderStatus.PRODUCING.getKey());
        update(order);
    }

    /**
     * 暂停生产
     *
     * @param id 单据ID
     */
    public final void pause(long id) {
        OrderEntity order = get(id);
        OrderStatus[] canPauseStatusList = {OrderStatus.PRODUCING};
        OrderStatus currentStatus = DictionaryUtil.getDictionary(OrderStatus.class, order.getStatus());
        FORBIDDEN.when(!Arrays.asList(canPauseStatusList).contains(currentStatus), "该单据状态无法暂停生产");
        order.setStatus(OrderStatus.PAUSED.getKey());
        update(order);
    }
}