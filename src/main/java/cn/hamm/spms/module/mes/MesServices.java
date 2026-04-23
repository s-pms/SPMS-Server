package cn.hamm.spms.module.mes;

import cn.hamm.spms.module.mes.bom.BomService;
import cn.hamm.spms.module.mes.bom.detail.BomDetailService;
import cn.hamm.spms.module.mes.operation.OperationService;
import cn.hamm.spms.module.mes.order.OrderService;
import cn.hamm.spms.module.mes.order.detail.OrderDetailService;
import cn.hamm.spms.module.mes.picking.PickingService;
import cn.hamm.spms.module.mes.picking.detail.PickingDetailService;
import cn.hamm.spms.module.mes.plan.PlanService;
import cn.hamm.spms.module.mes.plan.detail.PlanDetailService;
import cn.hamm.spms.module.mes.routing.RoutingService;
import cn.hamm.spms.module.mes.routing.operation.RoutingOperationService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <h1>服务整合助手类</h1>
 *
 * @author Hamm.cn
 */
@Component
public class MesServices {
    @Getter
    private static BomService bomService;

    @Getter
    private static BomDetailService bomDetailService;

    @Getter
    private static OperationService operationService;

    @Getter
    private static OrderService orderService;

    @Getter
    private static OrderDetailService orderDetailService;

    @Getter
    private static PickingService pickingService;

    @Getter
    private static PickingDetailService pickingDetailService;

    @Getter
    private static PlanService planService;

    @Getter
    private static PlanDetailService planDetailService;

    @Getter
    private static RoutingService routingService;

    @Getter
    private static RoutingOperationService routingOperationService;

    @Autowired
    private void initService(
            BomService bomService,
            BomDetailService bomDetailService,
            OperationService operationService,
            OrderService orderService,
            OrderDetailService orderDetailService,
            PickingService pickingService,
            PickingDetailService pickingDetailService,
            PlanService planService,
            PlanDetailService planDetailService,
            RoutingService routingService,
            RoutingOperationService routingOperationService
    ) {
        MesServices.bomService = bomService;
        MesServices.bomDetailService = bomDetailService;
        MesServices.operationService = operationService;
        MesServices.orderService = orderService;
        MesServices.orderDetailService = orderDetailService;
        MesServices.pickingService = pickingService;
        MesServices.pickingDetailService = pickingDetailService;
        MesServices.planService = planService;
        MesServices.planDetailService = planDetailService;
        MesServices.routingService = routingService;
        MesServices.routingOperationService = routingOperationService;
    }
}
