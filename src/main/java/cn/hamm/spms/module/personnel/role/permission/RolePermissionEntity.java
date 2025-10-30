package cn.hamm.spms.module.personnel.role.permission;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.module.personnel.role.RoleEntity;
import cn.hamm.spms.module.system.permission.PermissionEntity;
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
 * <h1>角色权限关系表</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "role_permission")
@Description("角色权限")
public class RolePermissionEntity extends BaseEntity<RolePermissionEntity> {
    @Description("角色")
    @ManyToOne
    @NotNull(groups = {WhenAdd.class}, message = "角色不能为空")
    private RoleEntity role;

    @Description("权限")
    @ManyToOne
    @NotNull(groups = {WhenAdd.class}, message = "权限不能为空")
    private PermissionEntity permission;
}
