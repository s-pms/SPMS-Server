package cn.hamm.spms.module.personnel.role;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.module.system.menu.MenuEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * <h1>角色与菜单的关联实体</h1>
 * <p>
 * 取代原先 {@code RoleEntity.menuList} 上的 ManyToMany 关联，
 * 原因与其他中间表实体一致。
 * </p>
 *
 * @author Hamm.cn
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
