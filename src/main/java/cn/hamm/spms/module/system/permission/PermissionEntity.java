package cn.hamm.spms.module.system.permission;

import cn.hamm.airpower.access.IPermission;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.dictionary.Dictionary;
import cn.hamm.airpower.meta.Meta;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.module.system.permission.enums.PermissionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.validator.constraints.Length;

import java.util.List;

/**
 * <h1>权限实体</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "permission")
@Description("权限")
public class PermissionEntity extends BaseEntity<PermissionEntity> implements IPermission<PermissionEntity> {
    @Description("权限标识")
    @Column(columnDefinition = "varchar(255) default '' comment '权限标识'", unique = true)
    @NotBlank(groups = {WhenUpdate.class, WhenAdd.class}, message = "权限标识不能为空")
    @Meta
    private String identity;

    @Description("系统权限")
    @Column(columnDefinition = "bit(1) default 0 comment '系统权限'")
    private Boolean isSystem;

    @Description("权限类型")
    @Column(columnDefinition = "int UNSIGNED default 0 comment '权限类型'")
    @Dictionary(value = PermissionType.class, groups = {WhenAdd.class, WhenUpdate.class})
    @Meta
    private Integer type;

    @Description("子菜单")
    @Transient
    private List<PermissionEntity> children;

    @Description("名称")
    @Search
    @Column(columnDefinition = "varchar(255) default '' comment '名称'")
    @Length(max = 200, message = "名称最多允许{max}个字符")
    @NotBlank(groups = {WhenUpdate.class, WhenAdd.class}, message = "名称不能为空")
    @Meta
    private String name;

    @Description("父级ID")
    @Column(columnDefinition = "bigint UNSIGNED default 0 comment '父级ID'")
    @Meta
    private Long parentId;
}
