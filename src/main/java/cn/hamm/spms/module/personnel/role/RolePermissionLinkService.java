package cn.hamm.spms.module.personnel.role;

import cn.hamm.spms.base.BaseService;
import lombok.extern.slf4j.Slf4j;
import cn.hamm.spms.module.system.permission.PermissionEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <h1>角色权限关联服务</h1>
 * <p>
 * 承载 {@code role <-> permission} 的多对多关系，取代 {@code @ManyToMany}。
 * </p>
 *
 * @author Hamm.cn
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
     * 同步某个角色的授权（增量同步）
     * <p>
     * 只解绑「本次提交里已不存在」的权限、只建立「本次新增」的授权。
     * </p>
     * <p>
     * 不使用 {@code repository.deleteAll} + {@code flush}：它们直接拼批量 SQL，
     * <b>不触发</b> JPA 实体生命周期回调、<b>不走</b> {@code beforeAppDelete}
     * 之类的业务钩子，也不做级联处理。删除一律走框架标准的 {@code delete(id)}。
     * </p>
     *
     * @param roleId      角色 ID
     * @param permissions 授权的权限
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
        // 走 service.delete 保证前后置钩子被触发
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
