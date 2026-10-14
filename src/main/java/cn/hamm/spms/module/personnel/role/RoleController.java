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

import java.util.Objects;

import static cn.hamm.airpower.exception.Errors.FORBIDDEN;

/**
 * <h1>角色</h1>
 *
 * @author Hamm.cn
 */
@Api("role")
@Description("角色")
@Extends({Curd.Disable, Curd.Enable})
public class RoleController extends BaseController<RoleEntity, RoleService, RoleRepository> implements IRoleAction {

    @Description("授权菜单")
    @PostMapping("authorizeMenu")
    public Json authorizeMenu(@RequestBody @Validated({WhenAuthorizeMenu.class, WhenIdRequired.class}) RoleEntity role) {
        service.authorizeMenu(role, role.getMenuList());
        return Json.success("授权菜单成功");
    }

    @Description("授权权限")
    @PostMapping("authorizePermission")
    public Json authorizePermission(@RequestBody @Validated({WhenAuthorizePermission.class, WhenIdRequired.class}) RoleEntity role) {
        service.authorizePermission(role, role.getPermissionList());
        return Json.success("授权权限成功");
    }

    /**
     * 通用 update 不允许改菜单与权限
     *
     * @param role  待更新的角色
     * @param exist 数据库中的角色
     * @return 处理后的角色
     * @apiNote 与 {@code RoleService.beforeUpdate} 的守卫重复，这里是给 HTTP 路径提前拦截，
     * Service 层那道才是不依赖调用方的真正守卫
     */
    @Override
    protected RoleEntity beforeAppUpdate(@NotNull RoleEntity role, @NotNull RoleEntity exist) {
        if (Objects.nonNull(role.getMenuList()) || Objects.nonNull(role.getPermissionList())) {
            FORBIDDEN.show("请使用「授权菜单」/「授权权限」接口修改角色的菜单与权限");
        }
        return role;
    }
}
