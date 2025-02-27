package cn.hamm.spms.common;

import cn.hamm.spms.common.config.AppConfig;
import cn.hamm.spms.module.asset.device.DeviceService;
import cn.hamm.spms.module.asset.material.MaterialService;
import cn.hamm.spms.module.channel.customer.CustomerService;
import cn.hamm.spms.module.channel.purchase.PurchaseService;
import cn.hamm.spms.module.channel.purchaseprice.PurchasePriceService;
import cn.hamm.spms.module.channel.sale.SaleService;
import cn.hamm.spms.module.channel.sale.detail.SaleDetailService;
import cn.hamm.spms.module.channel.saleprice.SalePriceService;
import cn.hamm.spms.module.channel.supplier.SupplierService;
import cn.hamm.spms.module.chat.room.RoomService;
import cn.hamm.spms.module.factory.storage.StorageService;
import cn.hamm.spms.module.factory.structure.StructureService;
import cn.hamm.spms.module.iot.parameter.ParameterService;
import cn.hamm.spms.module.mes.order.OrderService;
import cn.hamm.spms.module.mes.order.detail.OrderDetailService;
import cn.hamm.spms.module.mes.picking.PickingService;
import cn.hamm.spms.module.mes.picking.detail.PickingDetailService;
import cn.hamm.spms.module.mes.plan.PlanService;
import cn.hamm.spms.module.mes.plan.detail.PlanDetailService;
import cn.hamm.spms.module.mes.routing.operation.RoutingOperationService;
import cn.hamm.spms.module.open.app.OpenAppService;
import cn.hamm.spms.module.open.notify.NotifyService;
import cn.hamm.spms.module.personnel.user.UserService;
import cn.hamm.spms.module.personnel.user.department.DepartmentService;
import cn.hamm.spms.module.system.coderule.CodeRuleService;
import cn.hamm.spms.module.system.config.ConfigService;
import cn.hamm.spms.module.system.log.LogService;
import cn.hamm.spms.module.system.menu.MenuService;
import cn.hamm.spms.module.system.permission.PermissionService;
import cn.hamm.spms.module.system.unit.UnitService;
import cn.hamm.spms.module.wms.input.InputService;
import cn.hamm.spms.module.wms.inventory.InventoryService;
import cn.hamm.spms.module.wms.output.OutputService;
import cn.hamm.spms.module.wms.output.detail.OutputDetailService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <h1>服务整合助手类</h1>
 *
 * @author Hamm.cn
 */
@Component
public class Services {
    @Getter
    private static AppConfig appConfig;

    @Getter
    private static PurchaseService purchaseService;

    @Getter
    private static SaleService saleService;

    @Getter
    private static InputService inputService;

    @Getter
    private static OutputService outputService;

    @Getter
    private static CodeRuleService codeRuleService;

    @Getter
    private static MenuService menuService;

    @Getter
    private static PermissionService permissionService;

    @Getter
    private static ParameterService parameterService;

    @Getter
    private static DeviceService deviceService;

    @Getter
    private static UserService userService;

    @Getter
    private static MaterialService materialService;

    @Getter
    private static UnitService unitService;

    @Getter
    private static CustomerService customerService;

    @Getter
    private static SupplierService supplierService;

    @Getter
    private static SalePriceService salePriceService;

    @Getter
    private static PurchasePriceService purchasePriceService;

    @Getter
    private static StorageService storageService;

    @Getter
    private static StructureService structureService;

    @Getter
    private static InventoryService inventoryService;

    @Getter
    private static LogService logService;

    @Getter
    private static OpenAppService openAppService;

    @Getter
    private static NotifyService notifyService;

    @Getter
    private static DepartmentService departmentService;

    @Getter
    private static ConfigService configService;

    @Getter
    private static OrderDetailService orderDetailService;

    @Getter
    private static OrderService orderService;

    @Getter
    private static PlanService planService;

    @Getter
    private static PlanDetailService planDetailService;

    @Getter
    private static RoutingOperationService routingOperationService;

    @Getter
    private static PickingService pickingService;

    @Getter
    private static PickingDetailService pickingDetailService;

    @Getter
    private static SaleDetailService saleDetailService;

    @Getter
    private static OutputDetailService outputDetailService;

    @Getter
    private static RoomService roomService;

    @Autowired
    private void initService(
            CodeRuleService codeRuleService,
            PurchaseService purchaseService,
            SaleService saleService,
            InputService inputService,
            OutputService outputService,
            MenuService menuService,
            PermissionService permissionService,
            ParameterService parameterService,
            DeviceService deviceService,
            UserService userService,
            MaterialService materialService,
            UnitService unitService,
            SupplierService supplierService,
            CustomerService customerService,
            SalePriceService salePriceService,
            PurchasePriceService purchasePriceService,
            StorageService storageService,
            StructureService structureService,
            InventoryService inventoryService,
            LogService logService,
            AppConfig appConfig,
            OpenAppService openAppService,
            NotifyService notifyService,
            DepartmentService departmentService,
            ConfigService configService,
            OrderDetailService orderDetailService,
            OrderService orderService,
            PlanService planService,
            PlanDetailService planDetailService,
            RoutingOperationService routingOperationService,
            PickingService pickingService,
            PickingDetailService pickingDetailService,
            SaleDetailService saleDetailService,
            OutputDetailService outputDetailService,
            RoomService roomService
    ) {
        Services.codeRuleService = codeRuleService;
        Services.purchaseService = purchaseService;
        Services.saleService = saleService;
        Services.inputService = inputService;
        Services.outputService = outputService;
        Services.menuService = menuService;
        Services.permissionService = permissionService;
        Services.parameterService = parameterService;
        Services.deviceService = deviceService;
        Services.userService = userService;
        Services.materialService = materialService;
        Services.unitService = unitService;
        Services.supplierService = supplierService;
        Services.customerService = customerService;
        Services.salePriceService = salePriceService;
        Services.purchasePriceService = purchasePriceService;
        Services.storageService = storageService;
        Services.structureService = structureService;
        Services.inventoryService = inventoryService;
        Services.logService = logService;
        Services.appConfig = appConfig;
        Services.openAppService = openAppService;
        Services.notifyService = notifyService;
        Services.departmentService = departmentService;
        Services.configService = configService;
        Services.orderDetailService = orderDetailService;
        Services.orderService = orderService;
        Services.planService = planService;
        Services.planDetailService = planDetailService;
        Services.routingOperationService = routingOperationService;
        Services.pickingService = pickingService;
        Services.pickingDetailService = pickingDetailService;
        Services.saleDetailService = saleDetailService;
        Services.outputDetailService = outputDetailService;
        Services.roomService = roomService;
    }
}
