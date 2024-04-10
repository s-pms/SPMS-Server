package cn.hamm.spms.module.factory.structure;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.spms.base.BaseTreeEntity;
import cn.hamm.spms.common.annotation.AutoGenerateCode;
import cn.hamm.spms.module.system.coderule.CodeRuleField;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <h1>工厂结构实体</h1>
 *
 * @author Hamm
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@Table(name = "structure")
@Description("工厂结构")
public class StructureEntity extends BaseTreeEntity<StructureEntity> {
    @Description("工厂结构编码")
    @Column(columnDefinition = "varchar(255) default '' comment '工厂结构编码'", unique = true)
    @AutoGenerateCode(CodeRuleField.StructureCode)
    private String code;
}
