package cn.hamm.spms.module.system.menu;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.meta.Meta;
import cn.hamm.airpower.tree.ITree;
import cn.hamm.spms.base.BaseEntity;
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
@Table(name = "menu")
@Description("菜单")
public class MenuEntity extends BaseEntity<MenuEntity> implements ITree<MenuEntity> {
    @Description("名称")
    @Search
    @Column(columnDefinition = "varchar(255) default '' comment '名称'")
    @Length(max = 200, message = "名称最多允许{max}个字符")
    @NotBlank(groups = {WhenUpdate.class, WhenAdd.class}, message = "名称不能为空")
    @Meta
    private String name;

    @Description("父级 ID")
    @Column(columnDefinition = "bigint UNSIGNED default 0 comment '父级ID'")
    @Meta
    private Long parentId;

    @Description("菜单路径")
    @Column(columnDefinition = "varchar(255) default '' comment '菜单路径'")
    @Meta
    private String path;

    @Description("组件路径")
    @Column(columnDefinition = "varchar(255) default '' comment '组件路径'")
    @Meta
    private String component;

    @Description("菜单图标")
    @Column(columnDefinition = "varchar(255) default '' comment '菜单图标'")
    @Meta
    private String icon;

    @Description("排序号")
    @Column(columnDefinition = "int UNSIGNED default 0 comment '排序号'")
    @Meta
    private Integer orderNo;

    @Description("树子集节点数组")
    @Transient
    private List<MenuEntity> children;
}
