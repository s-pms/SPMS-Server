package cn.hamm.spms.module.personnel.role;

import cn.hamm.airpower.api.annotation.Api;
import cn.hamm.airpower.core.Json;
import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.curd.annotation.Extends;
import cn.hamm.airpower.curd.base.Curd;
import cn.hamm.spms.base.BaseController;
import org.jetbrains.annotations.NotNull;
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
@Extends({Curd.Disable, Curd.Enable})
public class RoleController extends BaseController<RoleEntity, RoleService, RoleRepository> implements IRoleAction {

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
//        role.getMenuList().size();
//        role.getPermissionList().size();
        return role;
    }

    @Override
    protected RoleEntity beforeAppUpdate(@NotNull RoleEntity role) {
        return role.setMenuList(null).setPermissionList(null);
    }
}
