package cn.hamm.spms.module.personnel.role.menu;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.module.personnel.role.RoleEntity;
import cn.hamm.spms.module.system.menu.MenuEntity;
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
@Table(name = "role_menu")
@Description("角色菜单")
public class RoleMenuEntity extends BaseEntity<RoleMenuEntity> {
    @Description("角色")
    @ManyToOne
    @NotNull(groups = {WhenAdd.class}, message = "角色不能为空")
    private RoleEntity role;

    @Description("菜单")
    @ManyToOne
    @NotNull(groups = {WhenAdd.class}, message = "菜单不能为空")
    private MenuEntity menu;
}
