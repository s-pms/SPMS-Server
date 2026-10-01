package cn.hamm.spms.module.personnel.role;

import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.personnel.PersonnelServices;
import cn.hamm.spms.module.system.menu.MenuEntity;
import cn.hamm.spms.module.system.permission.PermissionEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static cn.hamm.airpower.exception.Errors.FORBIDDEN;
import static cn.hamm.airpower.exception.Errors.PARAM_INVALID;

/**
 * <h1>角色</h1>
 *
 * @author Hamm.cn
 */
@Service
public class RoleService extends BaseService<RoleEntity, RoleRepository> {

    /**
     * 读取角色时组装授权的菜单与权限
     *
     * @param role 角色
     * @return 组装后的角色
     */
    @Override
    protected @NotNull RoleEntity afterAppGet(@NotNull RoleEntity role) {
        return fillLinks(role);
    }

    @Override
    protected @NotNull List<RoleEntity> afterGetList(@NotNull List<RoleEntity> list) {
        list.forEach(this::fillLinks);
        return list;
    }

    /**
     * 组装单个角色的授权
     *
     * @param role 角色
     * @return 组装后的角色
     */
    private @NotNull RoleEntity fillLinks(@NotNull RoleEntity role) {
        if (Objects.isNull(role.getId())) {
            return role;
        }
        Map<Long, Set<MenuEntity>> menuMap = PersonnelServices.getRoleMenuLinkService()
                .mapMenusByRoleIds(List.of(role.getId()));
        Map<Long, Set<PermissionEntity>> permissionMap = PersonnelServices.getRolePermissionLinkService()
                .mapPermissionsByRoleIds(List.of(role.getId()));
        role.setMenuList(new LinkedHashSet<>(menuMap.getOrDefault(role.getId(), Set.of())));
        role.setPermissionList(new LinkedHashSet<>(permissionMap.getOrDefault(role.getId(), Set.of())));
        return role;
    }

    /**
     * 批量组装一组角色的授权（一次查询，避免逐个角色查造成 N+1）
     *
     * @param roles 角色集合
     */
    public void fillLinksForRoles(@NotNull Set<RoleEntity> roles) {
        List<Long> roleIds = roles.stream()
                .map(RoleEntity::getId)
                .filter(Objects::nonNull)
                .toList();
        if (roleIds.isEmpty()) {
            return;
        }
        Map<Long, Set<MenuEntity>> menuMap = PersonnelServices.getRoleMenuLinkService()
                .mapMenusByRoleIds(roleIds);
        Map<Long, Set<PermissionEntity>> permissionMap = PersonnelServices.getRolePermissionLinkService()
                .mapPermissionsByRoleIds(roleIds);
        roles.forEach(role -> {
            if (Objects.isNull(role.getId())) {
                return;
            }
            role.setMenuList(new LinkedHashSet<>(menuMap.getOrDefault(role.getId(), Set.of())));
            role.setPermissionList(new LinkedHashSet<>(permissionMap.getOrDefault(role.getId(), Set.of())));
        });
    }

    /**
     * 通用 update 不允许改菜单与权限
     *
     * @param role 待更新的角色
     * @return 处理后的角色
     * @apiNote 必须走 {@link #authorizeMenu} / {@link #authorizePermission} 专用入口。
     * 守卫放在 Service 层是因为 Controller 的 {@code beforeAppUpdate} 拦不住直接调 Service 的路径；
     * 关联改成 {@code @Transient} 后不再由 JPA 自动持久化，漏掉守卫就等于静默丢授权
     */
    @Override
    protected @NotNull RoleEntity beforeUpdate(@NotNull RoleEntity role) {
        // 携带了关联字段就拒绝，**包括空集**：放行空集等于用通用 update 静默清空授权，绕过授权接口
        if (Objects.nonNull(role.getMenuList()) || Objects.nonNull(role.getPermissionList())) {
            FORBIDDEN.show("请使用「授权菜单」/「授权权限」接口修改角色的菜单与权限");
        }
        return role;
    }

    /**
     * 为角色授权菜单
     *
     * @param role     携带 ID 的角色实体
     * @param menuList 授权的菜单集合，传空集合表示清空授权
     * @apiNote 关联已改为 {@code role_menu_link} 中间表，这里直接同步中间表，
     * 不再依赖 JPA 自动维护多对多中间表
     */
    public void authorizeMenu(@NotNull RoleEntity role, @NotNull Set<MenuEntity> menuList) {
        PARAM_INVALID.whenNull(role.getId(), "角色ID不能为空");
        RoleEntity exist = get(role.getId());
        FORBIDDEN.when(Boolean.TRUE.equals(exist.getIsPublished()), "无法修改已经发布的数据");
        PersonnelServices.getRoleMenuLinkService().syncByRoleId(role.getId(),
                menuList.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(LinkedHashSet::new)));
        exist.setMenuList(new LinkedHashSet<>(menuList));
    }

    /**
     * 为角色授权权限
     *
     * @param role           携带 ID 的角色实体
     * @param permissionList 授权的权限集合，传空集合表示清空授权
     */
    public void authorizePermission(@NotNull RoleEntity role, @NotNull Set<PermissionEntity> permissionList) {
        PARAM_INVALID.whenNull(role.getId(), "角色ID不能为空");
        RoleEntity exist = get(role.getId());
        FORBIDDEN.when(Boolean.TRUE.equals(exist.getIsPublished()), "无法修改已经发布的数据");
        PersonnelServices.getRolePermissionLinkService().syncByRoleId(role.getId(),
                permissionList.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(LinkedHashSet::new)));
        exist.setPermissionList(new LinkedHashSet<>(permissionList));
    }
}
