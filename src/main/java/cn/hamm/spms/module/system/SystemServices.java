package cn.hamm.spms.module.system;

import cn.hamm.spms.module.system.coderule.CodeRuleService;
import cn.hamm.spms.module.system.config.ConfigService;
import cn.hamm.spms.module.system.file.FileService;
import cn.hamm.spms.module.system.menu.MenuService;
import cn.hamm.spms.module.system.permission.PermissionService;
import cn.hamm.spms.module.system.unit.UnitService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <h1>服务整合助手类</h1>
 *
 * @author Hamm.cn
 */
@Component
public class SystemServices {
    @Getter
    private static CodeRuleService codeRuleService;

    @Getter
    private static ConfigService configService;

    @Getter
    private static FileService fileService;

    @Getter
    private static MenuService menuService;

    @Getter
    private static PermissionService permissionService;

    @Getter
    private static UnitService unitService;

    @Autowired
    private void initService(
            CodeRuleService codeRuleService,
            ConfigService configService,
            FileService fileService,
            MenuService menuService,
            PermissionService permissionService,
            UnitService unitService
    ) {
        SystemServices.codeRuleService = codeRuleService;
        SystemServices.configService = configService;
        SystemServices.fileService = fileService;
        SystemServices.menuService = menuService;
        SystemServices.permissionService = permissionService;
        SystemServices.unitService = unitService;
    }
}
