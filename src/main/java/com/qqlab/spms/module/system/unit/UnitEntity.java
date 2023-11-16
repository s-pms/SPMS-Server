package com.qqlab.spms.module.system.unit;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Search;
import com.qqlab.spms.annotation.AutoGenerateCode;
import com.qqlab.spms.base.BaseEntity;
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
 * <h1>单位实体</h1>
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
@Table(name = "unit")
@Description("单位")
public class UnitEntity extends BaseEntity<UnitEntity> {
    /**
     * <h2>单位名称</h2>
     */
    @Description("单位名称")
    @Search(Search.Mode.EQUALS)
    @Column(columnDefinition = "varchar(255) default '' comment '单位名称'", unique = true)
    private String name;

    @Description("单位编码")
    @Column(columnDefinition = "varchar(255) default '' comment '单位编码'", unique = true)
    @AutoGenerateCode(CodeRuleField.UnitCode)
    private String code;
}