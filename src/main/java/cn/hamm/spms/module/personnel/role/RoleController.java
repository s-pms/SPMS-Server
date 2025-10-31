package cn.hamm.spms.module.personnel.role;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.api.Api;
import cn.hamm.airpower.api.Json;
import cn.hamm.spms.base.BaseController;
import cn.hamm.spms.module.personnel.role.menu.RoleMenuEntity;
import cn.hamm.spms.module.personnel.role.menu.RoleMenuService;
import cn.hamm.spms.module.personnel.role.permission.RolePermissionEntity;
import cn.hamm.spms.module.personnel.role.permission.RolePermissionService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("role")
@Description("角色")
public class RoleController extends BaseController<RoleEntity, RoleService, RoleRepository> implements IRoleAction {
    @Autowired
    private RoleMenuService roleMenuService;

    @Autowired
    private RolePermissionService rolePermissionService;

    @Description("授权菜单")
    @PostMapping("authorizeMenu")
    public Json authorizeMenu(@RequestBody @Validated({WhenAuthorizePermission.class, WhenIdRequired.class}) RoleEntity role) {
        service.update(role);
        return Json.success("授权菜单成功");
    }

    @Description("授权权限")
    @PostMapping("authorizePermission")
    public Json authorizePermission(@RequestBody @Validated({WhenAuthorizePermission.class, WhenIdRequired.class}) RoleEntity role) {
        service.update(role);
        return Json.success("授权菜单成功");
    }

    @Override
    protected RoleEntity afterGetDetail(@NotNull RoleEntity role) {
        return role.setMenuList(roleMenuService.getMenuList(role.getId()))
                .setPermissionList(rolePermissionService.getPermissionList(role.getId()))
                ;
    }

    @Override
    protected void afterAdd(long id, @NotNull RoleEntity source) {
        RoleEntity role = new RoleEntity().setId(id);
        source.getPermissionList().forEach(permission -> rolePermissionService.add(new RolePermissionEntity()
                .setRole(role)
                .setPermission(permission.copyOnlyId())
        ));
        source.getMenuList().forEach(menu -> roleMenuService.add(new RoleMenuEntity()
                .setRole(role)
                .setMenu(menu.copyOnlyId())
        ));
    }

    @Override
    protected void afterUpdate(long id, @NotNull RoleEntity source) {
        RoleEntity role = new RoleEntity().setId(id);
        rolePermissionService.filter(new RolePermissionEntity().setRole(role))
                .forEach(rolePermission -> rolePermissionService.delete(rolePermission.getId()));
        source.getPermissionList().forEach(permission -> rolePermissionService.add(new RolePermissionEntity()
                .setRole(role)
                .setPermission(permission.copyOnlyId())
        ));
        roleMenuService.filter(new RoleMenuEntity().setRole(role))
                .forEach(roleMenu -> roleMenuService.delete(roleMenu.getId()));
        source.getMenuList().forEach(menu -> roleMenuService.add(new RoleMenuEntity()
                .setRole(role)
                .setMenu(menu.copyOnlyId())
        ));
    }
}
