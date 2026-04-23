package cn.hamm.spms.module.personnel;

import cn.hamm.spms.module.personnel.department.DepartmentService;
import cn.hamm.spms.module.personnel.role.RoleService;
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

    @Autowired
    private void initService(
            DepartmentService departmentService,
            UserService userService,
            RoleService roleService
    ) {
        PersonnelServices.departmentService = departmentService;
        PersonnelServices.userService = userService;
        PersonnelServices.roleService = roleService;
    }
}
