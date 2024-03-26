package cn.hamm.spms.module.iot.parameter;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.ReadOnly;
import cn.hamm.airpower.annotation.Search;
import cn.hamm.airpower.validate.dictionary.Dictionary;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.module.iot.report.ReportDataType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * <h1>负载数据实体</h1>
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
@Table(name = "parameter")
@Description("采集参数")
public class ParameterEntity extends BaseEntity<ParameterEntity> {
    @Description("参数编码")
    @Column(columnDefinition = "varchar(255) default '' comment '参数编码'", unique = true)
    @NotBlank(groups = {WhenUpdate.class, WhenAdd.class})
    private String code;

    @Description("参数标题")
    @Column(columnDefinition = "varchar(255) default '' comment '参数标题'", unique = true)
    @NotBlank(groups = {WhenUpdate.class, WhenAdd.class}, message = "参数标题不能为空")
    private String label;

    @Description("内置参数")
    @Search(Search.Mode.EQUALS)
    @ReadOnly
    @Column(columnDefinition = "tinyint UNSIGNED default 0 comment '是否内置参数'")
    private Boolean isSystem;

    @Description("数据类型")
    @Search(Search.Mode.EQUALS)
    @Dictionary(value = ReportDataType.class,groups = {WhenAdd.class, WhenUpdate.class})
    @NotNull(groups = {WhenAdd.class, WhenUpdate.class}, message = "数据类型不允许为空")
    @Column(columnDefinition = "tinyint UNSIGNED default 0 comment '数据类型'")
    private Integer dataType;

    /**
     * 设置是系统内置参数
     *
     * @param isSystem 内置参数
     * @return 实体
     */
    public ParameterEntity setIsSystem(Boolean isSystem) {
        this.isSystem = isSystem;
        return this;
    }
}
