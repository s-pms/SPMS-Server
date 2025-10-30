package cn.hamm.spms.module.personnel.user.role;

import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.personnel.role.RoleEntity;
import cn.hamm.spms.module.personnel.user.UserEntity;
import lombok.extern.slf4j.Slf4j;
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
    public List<RoleEntity> getUserRoleList(long userId) {
        UserRoleEntity userRoleFilter = new UserRoleEntity().setUser(new UserEntity().setId(userId));
        return filter(userRoleFilter)
                .stream()
                .map(UserRoleEntity::getRole)
                .toList();
    }
}
