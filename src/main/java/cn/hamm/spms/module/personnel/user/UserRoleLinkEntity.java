package cn.hamm.spms.module.personnel.user;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.module.personnel.role.RoleEntity;
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
 * <h1>用户角色关联</h1>
 *
 * @author Hamm.cn
 * @apiNote 取代原先 {@code UserEntity.roleList} 上的 {@code @ManyToMany}：
 * 显式中间表可以携带额外字段（如「授权时间」「授权人」），隐式中间表做不到
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "user_role_link")
@Description("用户角色关联")
public class UserRoleLinkEntity extends BaseEntity<UserRoleLinkEntity> {

    @Description("用户")
    @ManyToOne(fetch = FetchType.EAGER)
    private UserEntity user;

    @Description("角色")
    @ManyToOne(fetch = FetchType.EAGER)
    private RoleEntity role;
}
