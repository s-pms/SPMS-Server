package cn.hamm.spms.module.personnel.role;

import cn.hamm.spms.base.BaseService;
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
     * 同步某个角色的授权（先删后建）
     * <p>
     * 必须用 {@code deleteAll} + {@code flush}，不能写成
     * {@code forEach(this::delete)}：逐条删除时，循环里下一条的
     * {@code get(id)} 查询会触发 auto-flush，最后一条的删除标记容易丢失。
     * </p>
     *
     * @param roleId      角色 ID
     * @param permissions 授权的权限
     */
    public void syncByRoleId(long roleId, @NotNull Collection<PermissionEntity> permissions) {
        List<RolePermissionLinkEntity> exists = repository.findByRoleId(roleId);
        if (!exists.isEmpty()) {
            repository.deleteAll(exists);
            repository.flush();
        }
        if (permissions.isEmpty()) {
            return;
        }
        RoleEntity role = new RoleEntity().setId(roleId);
        for (PermissionEntity permission : permissions) {
            if (Objects.nonNull(permission) && Objects.nonNull(permission.getId())) {
                addAndGet(new RolePermissionLinkEntity().setRole(role).setPermission(permission));
            }
        }
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
