package cn.hamm.spms.module.personnel.user.department;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.module.personnel.department.DepartmentEntity;
import cn.hamm.spms.module.personnel.user.UserEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * <h1>用户部门关系表</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "user_department")
@Description("用户部门")
public class UserDepartmentEntity extends BaseEntity<UserDepartmentEntity> {
    @Description("用户")
    @ManyToOne
    @NotNull(groups = {WhenAdd.class, WhenUpdate.class}, message = "用户不能为空")
    private UserEntity user;

    @Description("部门")
    @ManyToOne
    @NotNull(groups = {WhenAdd.class, WhenUpdate.class}, message = "部门不能为空")
    private DepartmentEntity department;
}
