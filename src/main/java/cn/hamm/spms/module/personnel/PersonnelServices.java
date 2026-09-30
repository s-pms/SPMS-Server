package cn.hamm.spms.module.personnel;

import cn.hamm.spms.module.personnel.department.DepartmentService;
import cn.hamm.spms.module.personnel.role.RoleMenuLinkService;
import cn.hamm.spms.module.personnel.role.RolePermissionLinkService;
import cn.hamm.spms.module.personnel.role.RoleService;
import cn.hamm.spms.module.personnel.user.UserRoleLinkService;
import cn.hamm.spms.module.personnel.user.UserService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <h1>服务整合助手类</h1>
 *
 * @author Hamm.cn
 */
@Component
public class PersonnelServices {

    @Getter
    private static DepartmentService departmentService;

    @Getter
    private static UserService userService;

    @Getter
    private static RoleService roleService;

    @Getter
    private static RoleMenuLinkService roleMenuLinkService;

    @Getter
    private static RolePermissionLinkService rolePermissionLinkService;

    @Getter
    private static UserRoleLinkService userRoleLinkService;

    @Autowired
    private void initService(
            DepartmentService departmentService,
            UserService userService,
            RoleService roleService,
            RoleMenuLinkService roleMenuLinkService,
            RolePermissionLinkService rolePermissionLinkService,
            UserRoleLinkService userRoleLinkService
    ) {
        PersonnelServices.departmentService = departmentService;
        PersonnelServices.userService = userService;
        PersonnelServices.roleService = roleService;
        PersonnelServices.roleMenuLinkService = roleMenuLinkService;
        PersonnelServices.rolePermissionLinkService = rolePermissionLinkService;
        PersonnelServices.userRoleLinkService = userRoleLinkService;
    }
}
