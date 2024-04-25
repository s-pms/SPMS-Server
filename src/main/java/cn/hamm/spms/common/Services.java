package cn.hamm.spms.common;

import cn.hamm.spms.module.asset.device.DeviceService;
import cn.hamm.spms.module.channel.purchase.PurchaseService;
import cn.hamm.spms.module.channel.sale.SaleService;
import cn.hamm.spms.module.iot.parameter.ParameterService;
import cn.hamm.spms.module.personnel.user.UserService;
import cn.hamm.spms.module.system.coderule.CodeRuleService;
import cn.hamm.spms.module.system.menu.MenuService;
import cn.hamm.spms.module.system.permission.PermissionService;
import cn.hamm.spms.module.wms.input.InputService;
import cn.hamm.spms.module.wms.output.OutputService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * <h1>服务整合助手类</h1>
 *
 * @author Hamm.cn
 */
@Component
@SuppressWarnings("UnusedReturnValue")
public class Services {
    @Getter
    private static Environment environment;

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

    @Autowired
    private void initService(
            Environment environment,
            CodeRuleService codeRuleService,
            PurchaseService purchaseService,
            SaleService saleService,
            InputService inputService,
            OutputService outputService,
            MenuService menuService,
            PermissionService permissionService,
            ParameterService parameterService,
            DeviceService deviceService,
            UserService userService
    ) {
        Services.environment = environment;
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
    }
}
