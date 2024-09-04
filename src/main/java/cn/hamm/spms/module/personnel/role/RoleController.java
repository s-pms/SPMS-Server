package cn.hamm.spms.module.personnel.role;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.model.Json;
import cn.hamm.spms.base.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("role")
@Description("角色")
public class RoleController extends BaseController<RoleEntity, RoleService, RoleRepository> implements IRoleAction {
    @Description("授权菜单")
    @RequestMapping("authorizeMenu")
    public Json authorizeMenu(@RequestBody @Validated({WhenAuthorizePermission.class, WhenIdRequired.class}) RoleEntity entity) {
        service.update(entity);
        return Json.success("授权菜单成功");
    }

    @Description("授权权限")
    @RequestMapping("authorizePermission")
    public Json authorizePermission(@RequestBody @Validated({WhenAuthorizePermission.class, WhenIdRequired.class}) RoleEntity entity) {
        service.update(entity);
        return Json.success("授权菜单成功");
    }

    @Override
    protected RoleEntity beforeUpdate(RoleEntity role) {
        return role.setMenuList(null).setPermissionList(null);
    }
}
