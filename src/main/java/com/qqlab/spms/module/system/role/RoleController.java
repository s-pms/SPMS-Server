package com.qqlab.spms.module.system.role;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.result.json.Json;
import cn.hamm.airpower.root.RootEntity;
import cn.hamm.airpower.root.RootEntityController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm
 */
@RestController
@RequestMapping("role")
@Description("角色")
public class RoleController extends RootEntityController<RoleService, RoleEntity> {
    @Description("授权菜单")
    @PostMapping("authorizeMenu")
    public Json authorizeMenu(@RequestBody @Validated({RoleEntity.WhenAuthorizePermission.class, RootEntity.WhenIdRequired.class}) RoleEntity entity) {
        service.update(entity);
        return json("授权菜单成功");
    }

    @Description("授权权限")
    @PostMapping("authorizePermission")
    public Json authorizePermission(@RequestBody @Validated({RoleEntity.WhenAuthorizePermission.class, RootEntity.WhenIdRequired.class}) RoleEntity entity) {
        service.update(entity);
        return json("授权菜单成功");
    }
}
