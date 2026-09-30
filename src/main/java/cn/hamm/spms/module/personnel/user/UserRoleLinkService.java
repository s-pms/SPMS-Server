package cn.hamm.spms.module.personnel.user;

import cn.hamm.spms.base.BaseService;
import lombok.extern.slf4j.Slf4j;
import cn.hamm.spms.module.personnel.role.RoleEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <h1>用户角色关联服务</h1>
 * <p>
 * 承载 {@code user <-> role} 的多对多关系，取代 {@code @ManyToMany}。
 * </p>
 *
 * @author Hamm.cn
 */
@Slf4j
@Service
public class UserRoleLinkService extends BaseService<UserRoleLinkEntity, UserRoleLinkRepository> {

    /**
     * 查出用户已分配的角色
     *
     * @param userId 用户 ID
     * @return 角色集合
     */
    public @NotNull Set<RoleEntity> getRoles(long userId) {
        return repository.findByUserId(userId).stream()
                .map(UserRoleLinkEntity::getRole)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * 同步用户的角色（增量）
     * <p>
     * 只解绑本次提交里已不存在的、只建立本次新增的。
     * </p>
     *
     * @param userId 用户 ID
     * @param roles  角色集合
     */
    public void syncByUserId(long userId, @NotNull Collection<RoleEntity> roles) {
        Set<Long> targetIds = roles.stream()
                .filter(Objects::nonNull)
                .map(RoleEntity::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<UserRoleLinkEntity> exists = repository.findByUserId(userId);
        UserEntity user = new UserEntity().setId(userId);
        Set<Long> kept = new LinkedHashSet<>();
        List<UserRoleLinkEntity> stale = new ArrayList<>();
        for (UserRoleLinkEntity link : exists) {
            Long roleId = Objects.isNull(link.getRole()) ? null : link.getRole().getId();
            if (Objects.isNull(roleId) || !targetIds.contains(roleId)) {
                stale.add(link);
            } else {
                kept.add(roleId);
            }
        }
        // 走 service.delete 保证前后置钩子被触发
        stale.forEach(entity -> delete(entity.getId()));
        roles.stream()
                .filter(Objects::nonNull)
                .filter(role -> Objects.nonNull(role.getId()))
                .filter(role -> !kept.contains(role.getId()))
                .forEach(role -> addAndGet(new UserRoleLinkEntity().setUser(user).setRole(role)));
        log.info("用户 {} 角色同步完成：原有 {} 个，现 {} 个", userId, exists.size(), targetIds.size());
    }
}
