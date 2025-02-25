package cn.hamm.spms.module.factory.structure;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.interfaces.ITree;
import cn.hamm.airpower.validate.dictionary.Dictionary;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.common.annotation.AutoGenerateCode;
import cn.hamm.spms.module.mes.operation.OperationEntity;
import cn.hamm.spms.module.system.coderule.CodeRuleField;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.validator.constraints.Length;

import java.util.List;
import java.util.Set;

/**
 * <h1>生产单元实体</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "structure")
@Description("生产单元")
public class StructureEntity extends BaseEntity<StructureEntity> implements ITree<StructureEntity> {
    @Description("生产单元编码")
    @Column(columnDefinition = "varchar(255) default '' comment '生产单元编码'", unique = true)
    @AutoGenerateCode(CodeRuleField.StructureCode)
    private String code;

    @Description("名称")
    @Search
    @Column(columnDefinition = "varchar(255) default '' comment '名称'")
    @Length(max = 200, message = "名称最多允许{max}个字符")
    @NotBlank(groups = {WhenUpdate.class, WhenAdd.class}, message = "名称不能为空")
    private String name;

    @Description("生产单元类型")
    @Search(Search.Mode.EQUALS)
    @Column(columnDefinition = "bigint UNSIGNED default 1 comment '生产单元类型'")
    @Dictionary(value = StructureType.class, groups = {WhenAdd.class, WhenUpdate.class})
    private Integer type;

    @Description("父级ID")
    @Column(columnDefinition = "bigint UNSIGNED default 0 comment '父级ID'")
    @Search(Search.Mode.EQUALS)
    private Long parentId;

    @Description("树子集节点数组")
    @Transient
    private List<StructureEntity> children;

    @Description("可执行工序")
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<OperationEntity> operationList;
}
