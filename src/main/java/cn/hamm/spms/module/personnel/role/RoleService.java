package cn.hamm.spms.module.personnel.role;

import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.system.menu.MenuEntity;
import cn.hamm.spms.module.system.permission.PermissionEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

import static cn.hamm.airpower.exception.Errors.FORBIDDEN;
import static cn.hamm.airpower.exception.Errors.PARAM_INVALID;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class RoleService extends BaseService<RoleEntity, RoleRepository> {

    /**
     * <h1>为角色授权菜单</h1>
     * <p>
     * 必须走 {@code updateToDatabase} 而非 {@code update}：
     * {@code update} 会先走 {@code beforeAppUpdate} 钩子，且只提交非 null 字段，
     * 集合字段在通用更新里会被忽略，导致授权静默失效。
     * </p>
     *
     * @param role       携带 id 与 menuList 的角色实体
     * @param menuList   授权的菜单集合，传空集合表示清空授权
     */
    public void authorizeMenu(@NotNull RoleEntity role, @NotNull Set<MenuEntity> menuList) {
        PARAM_INVALID.whenNull(role.getId(), "角色ID不能为空");
        RoleEntity exist = get(role.getId());
        FORBIDDEN.when(Boolean.TRUE.equals(exist.getIsPublished()), "无法修改已经发布的数据");
        // 必须转成可变集合，JPA 持久化关联时会往集合里增删元素
        updateToDatabase(exist.setMenuList(new HashSet<>(menuList)).setPermissionList(null));
    }

    /**
     * <h1>为角色授权权限</h1>
     *
     * @param role      携带 id 的角色实体
     * @param permissionList 授权的权限集合，传空集合表示清空授权
     */
    public void authorizePermission(@NotNull RoleEntity role, @NotNull Set<PermissionEntity> permissionList) {
        PARAM_INVALID.whenNull(role.getId(), "角色ID不能为空");
        RoleEntity exist = get(role.getId());
        FORBIDDEN.when(Boolean.TRUE.equals(exist.getIsPublished()), "无法修改已经发布的数据");
        // 必须转成可变集合，JPA 持久化关联时会往集合里增删元素
        updateToDatabase(exist.setPermissionList(new HashSet<>(permissionList)).setMenuList(null));
    }
}
