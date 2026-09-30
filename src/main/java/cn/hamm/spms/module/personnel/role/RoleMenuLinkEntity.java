package cn.hamm.spms.module.personnel.role;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.module.system.menu.MenuEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * <h1>角色菜单关联</h1>
 *
 * @author Hamm.cn
 * @apiNote 取代原先 {@code RoleEntity.menuList} 上的 {@code @ManyToMany}：
 * Hibernate 隐式生成的中间表结构不可控，也承载不了业务字段
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "role_menu_link")
@Description("角色菜单关联")
public class RoleMenuLinkEntity extends BaseEntity<RoleMenuLinkEntity> {

    @Description("角色")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;

    @Description("菜单")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "menu_id", nullable = false)
    private MenuEntity menu;
}
