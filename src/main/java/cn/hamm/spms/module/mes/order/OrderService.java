package cn.hamm.spms.module.mes.order;

import cn.hamm.airpower.exception.ServiceError;
import cn.hamm.airpower.interfaces.IDictionary;
import cn.hamm.airpower.util.DictionaryUtil;
import cn.hamm.airpower.util.NumberUtil;
import cn.hamm.spms.base.bill.AbstractBaseBillService;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.module.mes.order.detail.OrderDetailEntity;
import cn.hamm.spms.module.mes.order.detail.OrderDetailRepository;
import cn.hamm.spms.module.mes.order.detail.OrderDetailService;
import cn.hamm.spms.module.mes.plan.PlanEntity;
import cn.hamm.spms.module.mes.plan.PlanStatus;
import cn.hamm.spms.module.mes.plan.detail.PlanDetailEntity;
import cn.hamm.spms.module.mes.plan.detail.PlanDetailService;
import cn.hamm.spms.module.system.config.ConfigEntity;
import cn.hamm.spms.module.system.config.ConfigFlag;
import cn.hamm.spms.module.wms.input.InputEntity;
import cn.hamm.spms.module.wms.input.InputService;
import cn.hamm.spms.module.wms.input.InputType;
import cn.hamm.spms.module.wms.input.detail.InputDetailEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
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
    public IDictionary getFinishedStatus() {
        return OrderStatus.INPUTTING;
    }

    @Override
    protected ConfigFlag getManualFinishConfigFlag() {
        return ConfigFlag.ORDER_MANUAL_FINISH;
    }

    @Override
    public void addFinish(@NotNull OrderDetailEntity orderDetail) {
        ConfigEntity config = Services.getConfigService().get(ConfigFlag.ORDER_ENABLE_SUBMIT_WORK);
        ServiceError.FORBIDDEN.when(!config.booleanConfig(), "未开启订单报工模式");
        // 更新明细数量和状态
        orderDetail.setQuantity(orderDetail.getFinishQuantity()).setIsFinished(true);
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
            finish(order.getId());
        }
    }

    @Override
    protected void afterBillFinished(Long billId) {
        OrderEntity orderBill = get(billId);
        if (orderBill.getFinishQuantity() == 0) {
            // 直接完成 无需入库
            orderBill.setStatus(OrderStatus.DONE.getKey()).setFinishTime(System.currentTimeMillis());
            update(orderBill);
            return;
        }

        if (OrderType.PLAN.equalsKey(orderBill.getType())) {
            // 更新计划单
            updatePlanBill(orderBill);
        }
        // 添加入库单
        addInputBill(orderBill);
    }

    /**
     * <h2>添加入库单</h2>
     *
     * @param orderBill 订单
     */
    private void addInputBill(OrderEntity orderBill) {
        InputService inputService = Services.getInputService();
        InputEntity input = new InputEntity();
        input.setType(InputType.PRODUCTION.getKey());
        input.setOrder(orderBill);
        long inputId = inputService.add(input);

        List<InputDetailEntity> details = new ArrayList<>();
        details.add(new InputDetailEntity()
                .setQuantity(orderBill.getFinishQuantity())
                .setBillId(inputId)
                .setMaterial(orderBill.getMaterial())
        );
        input = inputService.get(inputId);
        input.setDetails(details);
        inputService.update(input);
    }

    /**
     * <h2>更新计划单</h2>
     *
     * @param orderBill 订单
     */
    private void updatePlanBill(@NotNull OrderEntity orderBill) {
        PlanEntity plan = orderBill.getPlan();
        double finishQuantity = orderBill.getFinishQuantity();
        PlanDetailService planDetailService = Services.getPlanDetailService();
        List<PlanDetailEntity> planDetails = planDetailService.getAllByBillId(plan.getId());
        for (PlanDetailEntity planDetail : planDetails) {
            if (finishQuantity <= 0) {
                break;
            }
            if (!planDetail.getMaterial().getId().equals(orderBill.getMaterial().getId())) {
                // 不操作其他物料
                continue;
            }
            if (planDetail.getIsFinished()) {
                // 不操作非生产中的
                continue;
            }

            // 还需要完成的数量
            double detailNeedQuantity = NumberUtil.sub(planDetail.getQuantity(), planDetail.getFinishQuantity());
            if (finishQuantity < detailNeedQuantity) {
                // 添加单据完成数量
                planDetail.setFinishQuantity(finishQuantity);
            } else {
                finishQuantity = NumberUtil.sub(finishQuantity, detailNeedQuantity);
                planDetail.setFinishQuantity(planDetail.getQuantity()).setIsFinished(true);
            }
            planDetailService.update(planDetail);
        }
        // 判断所有明细是否完成
        ConfigEntity configPlanAutoFinish = Services.getConfigService().get(ConfigFlag.PLAN_AUTO_FINISH);
        if (configPlanAutoFinish.booleanConfig()) {
            planDetails = planDetailService.getAllByBillId(plan.getId());
            boolean isAllFinished = planDetails.stream()
                    .allMatch(PlanDetailEntity::getIsFinished);
            if (isAllFinished) {
                // 明细已全部完成
                plan = Services.getPlanService().get(plan.getId());
                plan.setStatus(PlanStatus.DONE.getKey()).setFinishTime(System.currentTimeMillis());
                Services.getPlanService().update(plan);
            }
        }
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
     * <h3>开始生产</h3>
     *
     * @param id 单据ID
     */
    public final void start(long id) {
        OrderEntity order = get(id);
        OrderStatus[] canStartStatusList = {OrderStatus.PREPARE, OrderStatus.PAUSED};
        OrderStatus currentStatus = DictionaryUtil.getDictionary(OrderStatus.class, order.getStatus());
        ServiceError.FORBIDDEN.when(!Arrays.asList(canStartStatusList).contains(currentStatus), "该单据状态无法开始生产");
        order.setStatus(OrderStatus.PRODUCING.getKey());
        update(order);
    }

    /**
     * <h3>暂停生产</h3>
     *
     * @param id 单据ID
     */
    public final void pause(long id) {
        OrderEntity order = get(id);
        OrderStatus[] canStartStatusList = {OrderStatus.PRODUCING};
        OrderStatus currentStatus = DictionaryUtil.getDictionary(OrderStatus.class, order.getStatus());
        ServiceError.FORBIDDEN.when(!Arrays.asList(canStartStatusList).contains(currentStatus), "该单据状态无法暂停生产");
        order.setStatus(OrderStatus.PAUSED.getKey());
        update(order);
    }
}