package cn.hamm.spms.module.personnel.user;

import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.personnel.role.RoleEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

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
     * 同步用户的角色（先删后建）
     *
     * @param userId 用户 ID
     * @param roles  角色集合
     */
    public void syncByUserId(long userId, @NotNull Collection<RoleEntity> roles) {
        List<UserRoleLinkEntity> exists = repository.findByUserId(userId);
        if (!exists.isEmpty()) {
            repository.deleteAll(exists);
            repository.flush();
        }
        if (roles.isEmpty()) {
            return;
        }
        UserEntity user = new UserEntity().setId(userId);
        for (RoleEntity role : roles) {
            if (Objects.nonNull(role) && Objects.nonNull(role.getId())) {
                addAndGet(new UserRoleLinkEntity().setUser(user).setRole(role));
            }
        }
    }
}
