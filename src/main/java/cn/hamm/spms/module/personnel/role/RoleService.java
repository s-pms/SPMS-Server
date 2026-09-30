package cn.hamm.spms.module.personnel.role;

import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.personnel.PersonnelServices;
import cn.hamm.spms.module.system.menu.MenuEntity;
import cn.hamm.spms.module.system.permission.PermissionEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
     * 读取角色时组装授权的菜单与权限
     * <p>
     * 这两个集合已改为 {@code @Transient}（关联由中间表实体承载），
     * 必须显式组装后回填，否则 RBAC 鉴权会拿不到权限。
     * </p>
     * <p>
     * 组装放在事务内完成：{@code get} 本身没有事务，
     * 而 open-in-view 已关闭，事务外拿到的关联对象无法再触发懒加载。
     * </p>
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
     * @return 组装后的角色集合
     */
    public @NotNull Set<RoleEntity> fillLinksForRoles(@NotNull Set<RoleEntity> roles) {
        List<Long> roleIds = roles.stream()
                .map(RoleEntity::getId)
                .filter(Objects::nonNull)
                .toList();
        if (roleIds.isEmpty()) {
            return roles;
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
        return roles;
    }

    /**
     * 通用 update 不允许改菜单与权限
     * <p>
     * 必须走 {@link #authorizeMenu} / {@link #authorizePermission} 专用入口。
     * <p>
     * 守卫放在 Service 层而不是只在 Controller 层：Controller 的
     * {@code beforeAppUpdate} 拦不住直接调用 Service 的路径。改造中间表前
     * 这个用例是靠 {@code Set.of()} 不可变、JPA 往里 add 时抛异常才"通过"的，
     * 属于侥幸；关联改成 {@code @Transient} 后不再持久化，必须有真正的守卫。
     * </p>
     *
     * @param role 待更新的角色
     * @return 处理后的角色
     */
    @Override
    protected @NotNull RoleEntity beforeUpdate(@NotNull RoleEntity role) {
        // 只要携带了关联字段就拒绝，**包括空集**：
        // 「传空集」若被放行，等于用通用 update 静默清空授权，绕过授权接口。
        // 清空授权也必须走 authorizeMenu / authorizePermission。
        if (Objects.nonNull(role.getMenuList()) || Objects.nonNull(role.getPermissionList())) {
            FORBIDDEN.show("请使用「授权菜单」/「授权权限」接口修改角色的菜单与权限");
        }
        return role;
    }

    /**
     * <h1>为角色授权菜单</h1>
     * <p>
     * 菜单与权限的关联已改为中间表实体（{@code role_menu_link}），
     * 这里同步中间表；原先的 {@code updateToDatabase(exist.setMenuList(...))}
     * 依赖 JPA 自动维护多对多中间表，改造后不再适用。
     * </p>
     *
     * @param role     携带 id 的角色实体
     * @param menuList 授权的菜单集合，传空集合表示清空授权
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
     * <h1>为角色授权权限</h1>
     *
     * @param role           携带 id 的角色实体
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
