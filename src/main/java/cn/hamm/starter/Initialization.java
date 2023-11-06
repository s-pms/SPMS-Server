package cn.hamm.starter;

import cn.hamm.airpower.security.PasswordUtil;
import cn.hamm.starter.module.system.app.AppEntity;
import cn.hamm.starter.module.system.app.AppService;
import cn.hamm.starter.module.system.menu.MenuEntity;
import cn.hamm.starter.module.system.menu.MenuService;
import cn.hamm.starter.module.system.permission.PermissionService;
import cn.hamm.starter.module.system.role.RoleEntity;
import cn.hamm.starter.module.system.role.RoleService;
import cn.hamm.starter.module.system.user.UserEntity;
import cn.hamm.starter.module.system.user.UserService;
import cn.hutool.core.util.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * <h1>初始化</h1>
 *
 * @author hamm
 */
@Component
public class Initialization {
    private static UserService userService;
    private static RoleService roleService;
    private static PermissionService permissionService;
    private static AppService appService;
    private static MenuService menuService;

    public static void run() {
        initDatabase();
        permissionService.forceReloadAllPermissions();
    }

    private static void initDatabase() {
        // 初始化角色
        RoleEntity firstRole = roleService.getByIdMaybeNull(1L);
        if (Objects.nonNull(firstRole)) {
            return;
        }
        firstRole = roleService.add(new RoleEntity()
                .setName("超级管理员")
                .setIsSystem(true)
                .setRemark("超级管理员角色组,请勿数据库暴力直接删除"));

        // 初始化用户
        UserEntity userEntity = userService.getByIdMaybeNull(1L);
        if (Objects.nonNull(userEntity)) {
            return;
        }
        Set<RoleEntity> roleList = new HashSet<>();
        roleList.add(firstRole);
        String salt = RandomUtil.randomString(4);
        userService.add(new UserEntity()
                .setNickname("Hamm")
                .setEmail("admin@hamm.cn")
                .setPassword(PasswordUtil.encode("Aa123456", salt))
                .setSalt(salt)
                .setIsSystem(true)
                .setId(1L)
                .setRoleList(roleList)
                .setRemark("超级管理员,请勿数据库暴力直接删除"));

        System.out.println("---------------------------------");
        System.out.println("初始化第一个用户成功!\n");
        System.out.println("邮箱: admin@hamm.cn");
        System.out.println("密码: 12345678");

        // 初始化第三方应用
        appService.add(new AppEntity().setAppKey("airpower").setAppName("第三方应用").setUrl("https://hamm.cn").setAppSecret("abcdefghijklmnopqrstuvwxyz"));

        // 初始化菜单
        initMenu();
    }

    private static void initMenu() {
        MenuEntity exist = menuService.getByIdMaybeNull(1L);
        if (Objects.nonNull(exist)) {
            return;
        }
        MenuEntity homeMenu = new MenuEntity().setName("首页").setOrderNo(99).setPath("/console").setComponent("/console/index/index").setParentId(0L);
        menuService.add(homeMenu);

        MenuEntity userMenu = new MenuEntity().setName("人事管理").setOrderNo(88).setParentId(0L);
        userMenu = menuService.add(userMenu);

        MenuEntity userSubMenu;
        userSubMenu = new MenuEntity().setName("用户管理").setPath("/console/user/list").setParentId(userMenu.getId());
        menuService.add(userSubMenu);

        userSubMenu = new MenuEntity().setName("角色管理").setPath("/console/role/list").setParentId(userMenu.getId());
        menuService.add(userSubMenu);

        MenuEntity sourceMenu = new MenuEntity().setName("渠道管理").setOrderNo(77).setParentId(0L);
        sourceMenu = menuService.add(sourceMenu);

        MenuEntity sourceSubMenu;
        sourceSubMenu = new MenuEntity().setName("供应商管理").setPath("/console/supplier/list").setParentId(sourceMenu.getId());
        menuService.add(sourceSubMenu);

        MenuEntity sysMenu = new MenuEntity().setName("系统设置").setOrderNo(2).setParentId(0L);
        sysMenu = menuService.add(sysMenu);

        MenuEntity sysSubMenu;
        sysSubMenu = new MenuEntity().setName("权限管理").setPath("/console/permission/list").setParentId(sysMenu.getId());
        menuService.add(sysSubMenu);

        sysSubMenu = new MenuEntity().setName("菜单管理").setPath("/console/menu/list").setParentId(sysMenu.getId());
        menuService.add(sysSubMenu);

        sysSubMenu = new MenuEntity().setName("应用管理").setPath("/console/app/list").setParentId(sysMenu.getId());
        menuService.add(sysSubMenu);
    }

    @Autowired
    private void setDatastore(
            UserService userService,
            RoleService roleService,
            PermissionService permissionService,
            AppService appService,
            MenuService menuService
    ) {
        Initialization.userService = userService;
        Initialization.roleService = roleService;
        Initialization.permissionService = permissionService;
        Initialization.appService = appService;
        Initialization.menuService = menuService;
    }
}
