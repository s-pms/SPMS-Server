package cn.hamm.spms.module.system.coderule;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.ReadOnly;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.validate.dictionary.Dictionary;
import cn.hamm.spms.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

/**
 * <h1>编码规则实体</h1>
 *
 * @author Hamm
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "coderule")
@Description("编码规则")
public class CodeRuleEntity extends BaseEntity<CodeRuleEntity> {
    @Description("规则字段")
    @Column(columnDefinition = "tinyint UNSIGNED default 0 comment '规则字段'", unique = true)
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "规则字段不能为空")
    @Dictionary(value = CodeRuleField.class, groups = {WhenUpdate.class, WhenAdd.class})
    private Integer ruleField;

    @Description("流水号更新方式")
    @Column(columnDefinition = "tinyint UNSIGNED default 1 comment '流水号更新方式'")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "流水号更新方式不能为空")
    @Dictionary(value = SerialNumberUpdate.class)
    private Integer snType;

    @Description("流水号起始长度")
    @Column(columnDefinition = "tinyint UNSIGNED default 4 comment '流水号起始长度'")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "流水号起始长度不能为空")
    @Range(min = 1, max = 10, message = "流水号只允许{min}-{max}位")
    private Integer snLength;

    @Description("编码前缀")
    @Column(columnDefinition = "varchar(255) default '' comment '编码前缀'")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "流水号起始长度不能为空")
    @Length(min = 1, max = 10, message = "编码前缀只允许{min}-{max}位")
    private String prefix;

    @Description("编码规则模板")
    @Column(columnDefinition = "varchar(255) default '' comment '编码规则模板'")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "编码规则模板")
    @Length(max = 64, message = "模板最多允许{max}个字符")
    private String template;

    @Description("当前序列号")
    @ReadOnly
    @Column(columnDefinition = "int UNSIGNED default 0 comment '当前序列号'")
    private Integer currentSn;

    @Description("内置参数")
    @Search(Search.Mode.EQUALS)
    @ReadOnly
    @Column(columnDefinition = "tinyint UNSIGNED default 0 comment '是否内置参数'")
    private Boolean isSystem;

    /**
     * <h2>设置是系统内置规则</h2>
     *
     * @param isSystem 内置规则
     * @return 编码规则
     */
    public CodeRuleEntity setIsSystem(Boolean isSystem) {
        this.isSystem = isSystem;
        return this;
    }
}
