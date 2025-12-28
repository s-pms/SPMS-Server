package cn.hamm.spms.module.personnel.user.department;

import cn.hamm.airpower.exception.ServiceError;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.personnel.department.DepartmentEntity;
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
public class UserDepartmentService extends BaseService<UserDepartmentEntity, UserDepartmentRepository> {
    /**
     * 获取用户部门列表
     *
     * @param userId 用户 ID
     * @return 部门列表
     */
    public List<DepartmentEntity> getDepartmentList(long userId) {
        UserDepartmentEntity userDepartmentFilter = new UserDepartmentEntity().setUser(
                new UserEntity().setId(userId)
        );
        return filter(userDepartmentFilter)
                .stream()
                .map(UserDepartmentEntity::getDepartment)
                .toList();
    }

    @Override
    protected @NotNull UserDepartmentEntity beforeAdd(@NotNull UserDepartmentEntity userDepartment) {
        List<UserDepartmentEntity> exist = filter(new UserDepartmentEntity()
                .setUser(userDepartment.getUser().copyOnlyId())
                .setDepartment(userDepartment.getDepartment().copyOnlyId())
        );
        ServiceError.FORBIDDEN.when(!exist.isEmpty(), "添加失败，该用户已存在相同部门！");
        return userDepartment;
    }
}
