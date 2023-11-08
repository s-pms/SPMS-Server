package com.qqlab.spms.module.factory.structure;

import cn.hamm.airpower.annotation.Description;
import com.qqlab.spms.annotation.AutoGenerateCode;
import com.qqlab.spms.base.BaseTreeEntity;
import com.qqlab.spms.module.system.coderule.CodeRuleField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * <h1>工厂结构实体</h1>
 *
 * @author Hamm
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Table(name = "structure")
@Description("工厂结构")
public class StructureEntity extends BaseTreeEntity<StructureEntity> {
    @Description("工厂结构编码")
    @Column(columnDefinition = "varchar(255) default '' comment '工厂结构编码'", unique = true)
    @AutoGenerateCode(CodeRuleField.StructureCode)
    private String code;
}
