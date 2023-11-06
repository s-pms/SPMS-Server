package com.qqlab.spms.module.system.coderule;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.validate.dictionary.Dictionary;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qqlab.spms.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * <h1>编码规则实体</h1>
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
@Table(name = "coderule")
@Description("编码规则")
public class CodeRuleEntity extends BaseEntity<CodeRuleEntity> {
    /**
     * <h2>所属表</h2>
     */
    @Description("所属表")
    @Column(columnDefinition = "tinyint UNSIGNED default 0 comment '所属表'", unique = true)
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "所属表不能为空")
    @Dictionary(value = CodeRuleTable.class, groups = {WhenUpdate.class, WhenAdd.class})
    private Integer tableId;

    /**
     * <h2>流水号更新方式</h2>
     */
    @Description("流水号更新方式")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '流水号更新方式'")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "流水号更新方式不能为空")
    @Dictionary(value = SerialNumberUpdate.class, groups = {WhenUpdate.class, WhenAdd.class})
    private Integer snType;

    /**
     * <h2>流水号起始长度</h2>
     */
    @Description("流水号起始长度")
    @Column(columnDefinition = "tinyint UNSIGNED default 4 comment '流水号起始长度'")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "流水号起始长度不能为空")
    @Range(min = 4, max = 16, message = "流水号只允许{min}-{max}位")
    private Integer snLength;

    /**
     * <h2>编码前缀</h2>
     */
    @Description("编码前缀")
    @Column(columnDefinition = "varchar(255) default '' comment '编码前缀'")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "流水号起始长度不能为空")
    @Length(min = 1, max = 6, message = "编码前缀只允许{min}-{max}位")
    private String prefix;


    /**
     * <h2>编码规则模板</h2>
     */
    @Description("编码规则模板")
    @Column(columnDefinition = "varchar(255) default '' comment '编码规则模板'")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "编码规则模板")
    private String template;

    /**
     * <h2>当前序列号</h2>
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(columnDefinition = "int UNSIGNED default 0 comment '当前序列号'")
    private Integer currentSn;
}
