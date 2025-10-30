package cn.hamm.spms.module.personnel.role.permission;

import cn.hamm.airpower.access.Permission;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.api.Api;
import cn.hamm.airpower.api.Json;
import cn.hamm.airpower.curd.query.QueryListRequest;
import cn.hamm.spms.base.BaseController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("role/permission")
@Description("角色权限")
public class RolePermissionController extends BaseController<RolePermissionEntity, RolePermissionService, RolePermissionRepository> {
    @Permission(authorize = false)
    @Override
    public Json getList(QueryListRequest<RolePermissionEntity> queryListRequest) {
        return super.getList(queryListRequest);
    }
}
