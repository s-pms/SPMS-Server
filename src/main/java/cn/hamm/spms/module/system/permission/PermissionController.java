package cn.hamm.spms.module.system.permission;

import cn.hamm.airpower.core.TreeUtil;
import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.api.Api;
import cn.hamm.spms.base.BaseController;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("permission")
@Description("权限")
public class PermissionController extends BaseController<PermissionEntity, PermissionService, PermissionRepository> {

    @Override
    protected PermissionEntity beforeAdd(@NotNull PermissionEntity permission) {
        return permission.setIsSystem(null);
    }

    @Override
    protected PermissionEntity beforeAppUpdate(@NotNull PermissionEntity permission) {
        return permission.setIsSystem(null);
    }

    @Override
    protected List<PermissionEntity> afterGetList(List<PermissionEntity> list) {
        return TreeUtil.buildTreeList(list);
    }
}
