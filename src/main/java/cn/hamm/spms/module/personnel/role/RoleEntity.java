package cn.hamm.spms.module.personnel.role;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.core.annotation.Meta;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.common.annotation.AutoGenerateCode;
import cn.hamm.spms.module.system.menu.MenuEntity;
import cn.hamm.spms.module.system.permission.PermissionEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.Set;

import static cn.hamm.spms.module.system.coderule.enums.CodeRuleField.RoleCode;

/**
 * <h1>角色</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "role")
@Description("角色")
public class RoleEntity extends BaseEntity<RoleEntity> implements IRoleAction {
    @Description("角色名称")
    @Column(columnDefinition = "varchar(255) default '' comment '角色名称'", unique = true)
    @NotBlank(groups = {WhenUpdate.class, WhenAdd.class}, message = "角色名称不能为空")
    @Meta
    private String name;

    @Description("角色编码")
    @Column(columnDefinition = "varchar(255) default '' comment '角色编码'", unique = true)
    @AutoGenerateCode(RoleCode)
    @Meta
    private String code;

    /**
     * 授权的菜单
     *
     * @apiNote 关联关系由 {@code role_menu_link} 中间表承载，本字段只用于承接前端提交的 JSON
     * 与回传读取结果，不参与持久化；读写都由 {@code RoleService} 走中间表服务同步
     */
    @Description("授权菜单")
    @Transient
    @NotNull(groups = {WhenAuthorizeMenu.class}, message = "授权菜单不能为空")
    private Set<MenuEntity> menuList;

    /**
     * 授权的权限
     *
     * @apiNote 关联关系由 {@code role_permission_link} 中间表承载，含义同 {@link #menuList}
     */
    @Description("授权权限")
    @Transient
    @NotNull(groups = {WhenAuthorizePermission.class}, message = "授权权限不能为空")
    private Set<PermissionEntity> permissionList;
}
