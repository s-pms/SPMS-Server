package cn.hamm.spms.module.system.permission;

import cn.hamm.airpower.api.annotation.Api;
import cn.hamm.airpower.core.TreeUtil;
import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.spms.base.BaseController;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * <h1>权限</h1>
 *
 * @author Hamm.cn
 */
@Api("permission")
@Description("权限")
public class PermissionController extends BaseController<PermissionEntity, PermissionService, PermissionRepository> {

    /**
     * 丢弃客户端传入的系统权限标记
     *
     * @param permission 权限
     * @return 处理后的权限
     * @apiNote {@code isSystem} 只由 {@code PermissionService.loadPermission} 维护，
     * 客户端伪造就能把权限变成不可删的内置项
     */
    @Override
    protected PermissionEntity beforeAdd(@NotNull PermissionEntity permission) {
        return permission.setIsSystem(null);
    }

    /**
     * 丢弃客户端传入的系统权限标记
     *
     * @param permission 待更新的权限
     * @param exist      库中已有的权限
     * @return 处理后的权限
     * @apiNote 同 {@link #beforeAdd}，防止客户端把权限改成不可删的内置项
     */
    @Override
    protected PermissionEntity beforeAppUpdate(@NotNull PermissionEntity permission, @NotNull PermissionEntity exist) {
        return permission.setIsSystem(null);
    }

    @Override
    protected List<PermissionEntity> afterGetList(List<PermissionEntity> list) {
        return TreeUtil.buildTreeList(list);
    }
}
