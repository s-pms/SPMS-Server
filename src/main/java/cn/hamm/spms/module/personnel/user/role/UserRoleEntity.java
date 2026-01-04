package cn.hamm.spms.module.personnel.user.role;

import cn.hamm.airpower.util.annotation.Description;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.module.personnel.role.RoleEntity;
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
 * <h1>用户角色关系表</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "user_role")
@Description("用户角色")
public class UserRoleEntity extends BaseEntity<UserRoleEntity> {
    @Description("用户")
    @ManyToOne
    @NotNull(groups = {WhenAdd.class, WhenUpdate.class}, message = "用户不能为空")
    private UserEntity user;

    @Description("角色")
    @ManyToOne
    @NotNull(groups = {WhenAdd.class, WhenUpdate.class}, message = "角色不能为空")
    private RoleEntity role;
}
