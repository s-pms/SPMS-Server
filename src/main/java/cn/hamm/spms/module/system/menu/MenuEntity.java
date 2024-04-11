package cn.hamm.spms.module.system.menu;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Exclude;
import cn.hamm.spms.base.BaseTreeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * <h1>权限实体</h1>
 *
 * @author Hamm
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "menu")
@Description("菜单")
public class MenuEntity extends BaseTreeEntity<MenuEntity> {
    /**
     * 菜单路径
     */
    @Description("菜单路径")
    @NotBlank(groups = {WhenUpdate.class, WhenAdd.class}, message = "菜单路径不能为空")
    @Column(columnDefinition = "varchar(255) default '' comment '菜单路径'")
    private String path;

    /**
     * 组件路径
     */
    @Description("组件路径")
    @Column(columnDefinition = "varchar(255) default '' comment '组件路径'")
    private String component;

    /**
     * 菜单图标
     */
    @Description("菜单图标")
    @Column(columnDefinition = "varchar(255) default '' comment '菜单图标'")
    private String icon;

    /**
     * 排序号
     */
    @Description("排序号")
    @Column(columnDefinition = "tinyint UNSIGNED default 0 comment '排序号'")
    @Exclude(filters = {WhenPayLoad.class})
    private Integer orderNo;
}
