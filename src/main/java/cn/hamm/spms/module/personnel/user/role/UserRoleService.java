package cn.hamm.spms.module.personnel.user.role;

import cn.hamm.airpower.exception.ServiceError;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.personnel.role.RoleEntity;
import cn.hamm.spms.module.personnel.user.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Slf4j
@Service
public class UserRoleService extends BaseService<UserRoleEntity, UserRoleRepository> {
    /**
     * 获取用户的角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    public List<RoleEntity> getRoleList(long userId) {
        UserRoleEntity userRoleFilter = new UserRoleEntity().setUser(new UserEntity().setId(userId));
        return filter(userRoleFilter)
                .stream()
                .map(UserRoleEntity::getRole)
                .toList();
    }

    @Override
    protected @NotNull UserRoleEntity beforeAdd(@NotNull UserRoleEntity userRole) {
        List<UserRoleEntity> exist = filter(new UserRoleEntity()
                .setUser(userRole.getUser().copyOnlyId())
                .setRole(userRole.getRole().copyOnlyId())
        );
        ServiceError.FORBIDDEN.when(!exist.isEmpty(), "添加失败，该用户已存在相同角色！");
        return userRole;
    }
}
