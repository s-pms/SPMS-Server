package cn.hamm.spms.module.personnel.user.department;

import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.personnel.department.DepartmentEntity;
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
public class UserDepartmentService extends BaseService<UserDepartmentEntity, UserDepartmentRepository> {
    /**
     * 获取用户部门列表
     *
     * @param userId 用户ID
     * @return 部门列表
     */
    public List<DepartmentEntity> getUserDepartmentList(long userId) {
        UserDepartmentEntity userDepartmentFilter = new UserDepartmentEntity().setUser(
                new UserEntity().setId(userId)
        );
        return filter(userDepartmentFilter)
                .stream()
                .map(UserDepartmentEntity::getDepartment)
                .toList();
    }
}
