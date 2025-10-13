package cn.hamm.spms.module.personnel.department;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.tree.ITree;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.common.annotation.AutoGenerateCode;
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

import java.util.List;

import static cn.hamm.spms.module.system.coderule.enums.CodeRuleField.DepartmentCode;

/**
 * <h1>实体</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "department")
@Description("部门")
public class DepartmentEntity extends BaseEntity<DepartmentEntity> implements ITree<DepartmentEntity> {
    @Description("部门编码")
    @Column(columnDefinition = "varchar(255) default '' comment 'code'", unique = true)
    @AutoGenerateCode(DepartmentCode)
    private String code;

    @Description("部门名称")
    @Column(columnDefinition = "varchar(255) default '' comment '部门名称'")
    @NotBlank(groups = {WhenUpdate.class, WhenAdd.class}, message = "应用名称不能为空")
    @Search
    private String name;

    @Description("父级ID")
    @Column(columnDefinition = "bigint UNSIGNED default 0 comment '父级ID'")
    private Long parentId;

    @Description("排序号")
    @Column(columnDefinition = "tinyint UNSIGNED default 0 comment '排序号'")
    private Integer orderNo;

    @Description("树子集节点数组")
    @Transient
    private List<DepartmentEntity> children;
}
