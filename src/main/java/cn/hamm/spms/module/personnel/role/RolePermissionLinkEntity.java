package cn.hamm.spms.module.personnel.role;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.module.system.permission.PermissionEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * <h1>角色权限关联</h1>
 *
 * @author Hamm.cn
 * @apiNote 取代原先 {@code RoleEntity.permissionList} 上的 {@code @ManyToMany}：
 * Hibernate 隐式生成的中间表结构不可控，也承载不了业务字段
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "role_permission_link")
@Description("角色权限关联")
public class RolePermissionLinkEntity extends BaseEntity<RolePermissionLinkEntity> {

    @Description("角色")
    @ManyToOne(fetch = FetchType.EAGER)
    private RoleEntity role;

    @Description("权限")
    @ManyToOne(fetch = FetchType.EAGER)
    private PermissionEntity permission;
}
