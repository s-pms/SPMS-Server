package cn.hamm.spms.module.mes.order;

import cn.hamm.airpower.exception.ServiceError;
import cn.hamm.airpower.interfaces.IDictionary;
import cn.hamm.airpower.util.NumberUtil;
import cn.hamm.spms.base.bill.AbstractBaseBillService;
import cn.hamm.spms.base.bill.detail.BillDetailStatus;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.mes.order.detail.OrderDetailEntity;
import cn.hamm.spms.module.mes.order.detail.OrderDetailRepository;
import cn.hamm.spms.module.mes.order.detail.OrderDetailService;
import cn.hamm.spms.module.system.config.ConfigEntity;
import cn.hamm.spms.module.system.config.ConfigFlag;
import cn.hamm.spms.module.wms.input.InputEntity;
import cn.hamm.spms.module.wms.input.InputType;
import cn.hamm.spms.module.wms.input.detail.InputDetailEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class OrderService extends AbstractBaseBillService<OrderEntity, OrderRepository, OrderDetailEntity, OrderDetailService, OrderDetailRepository> {
    @Override
    public IDictionary getAuditedStatus() {
        return OrderStatus.PRODUCING;
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
    public void addFinish(@NotNull OrderDetailEntity orderDetail) {
        ConfigEntity config = Services.getConfigService().get(ConfigFlag.ORDER_ENABLE_SUBMIT_WORK);
        ServiceError.FORBIDDEN.when(!config.booleanConfig(), "未开启订单报工模式");
        // 更新明细数量和状态
        orderDetail.setQuantity(orderDetail.getFinishQuantity());
        orderDetail.setStatus(BillDetailStatus.FINISHED.getKey());
        Services.getOrderDetailService().add(orderDetail);

        // 更新订单数量
        OrderEntity order = get(orderDetail.getBillId());
        List<OrderDetailEntity> details = Services.getOrderDetailService().getAllByBillId(order.getId());
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

        config = Services.getConfigService().get(ConfigFlag.ORDER_AUTO_FINISH);
        if (config.booleanConfig() && order.getFinishQuantity() >= order.getQuantity()) {
            order.setStatus(OrderStatus.INPUTTING.getKey());
            update(order);
        }
    }

    /**
     * <h3>标记订单完成</h3>
     *
     * @param id 订单ID
     */
    public void finish(long id) {
        ConfigEntity config = Services.getConfigService().get(ConfigFlag.ORDER_MANUAL_FINISH);
        ServiceError.FORBIDDEN.when(!config.booleanConfig(), "未开启手动完成订单");

        OrderEntity order = get(id);
        ServiceError.FORBIDDEN.when(OrderStatus.PRODUCING.notEqualsKey(order.getStatus()), "该订单状态无法标记完成");
        if (order.getFinishQuantity() == 0) {
            order.setStatus(OrderStatus.DONE.getKey());
            update(order);
            return;
        }
        order.setStatus(OrderStatus.INPUTTING.getKey());
        update(order);

        // 添加入库单
        InputEntity input = new InputEntity();
        input.setType(InputType.PRODUCTION.getKey());
        input.setOrder(order);
        long inputId = Services.getInputService().add(input);

        List<InputDetailEntity> details = new ArrayList<>();
        details.add(new InputDetailEntity()
                .setQuantity(order.getFinishQuantity())
                .setBillId(inputId)
                .setMaterial(order.getMaterial())
        );
        input = Services.getInputService().get(inputId);
        input.setDetails(details);
        Services.getInputService().update(input);
    }

    @Override
    protected @NotNull OrderEntity beforeAdd(@NotNull OrderEntity order) {
        order.setDetails(new ArrayList<>());
        return order;
    }
}