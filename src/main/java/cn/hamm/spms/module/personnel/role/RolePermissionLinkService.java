package cn.hamm.spms.module.personnel.role;

import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.system.permission.PermissionEntity;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <h1>角色权限关联</h1>
 *
 * @author Hamm.cn
 * @apiNote 承载 {@code role <-> permission} 的多对多关系，取代 {@code @ManyToMany}：
 * RBAC 鉴权要在事务外读权限，{@code @ManyToMany} 的懒加载在 open-in-view 关闭时不可用
 */
@Slf4j
@Service
public class RolePermissionLinkService extends BaseService<RolePermissionLinkEntity, RolePermissionLinkRepository> {

    /**
     * 查出每个角色已授权的权限
     *
     * @param roleIds 角色 ID 集合
     * @return 角色 ID -> 权限集合
     */
    public @NotNull Map<Long, Set<PermissionEntity>> mapPermissionsByRoleIds(@NotNull Collection<Long> roleIds) {
        if (roleIds.isEmpty()) {
            return Map.of();
        }
        return repository.findByRoleIds(new ArrayList<>(roleIds)).stream()
                .collect(Collectors.groupingBy(
                        l -> l.getRole().getId(),
                        Collectors.mapping(
                                RolePermissionLinkEntity::getPermission,
                                Collectors.toCollection(LinkedHashSet::new))));
    }

    /**
     * 同步某个角色的授权
     *
     * @param roleId      角色 ID
     * @param permissions 授权的权限
     * @apiNote 增量同步：只解绑本次提交里已不存在的、只新增本次新增的，
     * 不做全删重建，避免并发下互相覆盖
     */
    public void syncByRoleId(long roleId, @NotNull Collection<PermissionEntity> permissions) {
        Set<Long> targetIds = permissions.stream()
                .filter(Objects::nonNull)
                .map(PermissionEntity::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<RolePermissionLinkEntity> exists = repository.findByRoleId(roleId);
        RoleEntity role = new RoleEntity().setId(roleId);
        Set<Long> kept = new LinkedHashSet<>();
        List<RolePermissionLinkEntity> stale = new ArrayList<>();
        for (RolePermissionLinkEntity link : exists) {
            Long permissionId = Objects.isNull(link.getPermission()) ? null : link.getPermission().getId();
            if (Objects.isNull(permissionId) || !targetIds.contains(permissionId)) {
                stale.add(link);
            } else {
                kept.add(permissionId);
            }
        }
        // 走 service.delete 保证前后置钩子被触发，直接调 repository 会跳过
        stale.forEach(entity -> delete(entity.getId()));
        permissions.stream()
                .filter(Objects::nonNull)
                .filter(permission -> Objects.nonNull(permission.getId()))
                .filter(permission -> !kept.contains(permission.getId()))
                .forEach(permission -> addAndGet(
                        new RolePermissionLinkEntity().setRole(role).setPermission(permission)));
        log.info("角色 {} 权限同步完成：原有 {} 个，现 {} 个", roleId, exists.size(), targetIds.size());
    }

    /**
     * 收集一组角色的全部权限（去重）
     *
     * @param roles 角色集合
     * @return 权限集合
     */
    public @NotNull Set<PermissionEntity> collectPermissions(@NotNull Collection<RoleEntity> roles) {
        return roles.stream()
                .map(role -> role.getPermissionList())
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
