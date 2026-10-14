package cn.hamm.spms.module.iot.parameter;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.core.annotation.Dictionary;
import cn.hamm.airpower.core.annotation.Meta;
import cn.hamm.airpower.core.annotation.ReadOnly;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.module.iot.report.enums.ReportDataType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * <h1>采集参数</h1>
 *
 * @author Hamm.cn
 * @apiNote {@code dataType} 绑定的是 {@link ReportDataType}（数量/状态/开关/信息），
 * 与同包下的 {@code ParameterType} 不是同一套字典
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "parameter")
@Description("采集参数")
public class ParameterEntity extends BaseEntity<ParameterEntity> {
    @Description("参数编码")
    @Column(columnDefinition = "varchar(255) default '' comment '参数编码'", unique = true)
    @NotBlank(groups = {WhenUpdate.class, WhenAdd.class})
    @Meta
    private String code;

    @Description("参数标题")
    @Column(columnDefinition = "varchar(255) default '' comment '参数标题'", unique = true)
    @NotBlank(groups = {WhenUpdate.class, WhenAdd.class}, message = "参数标题不能为空")
    @Meta
    private String label;

    @Description("内置参数")
    @ReadOnly
    @Column(columnDefinition = "bit(1) default 0 comment '是否内置参数'")
    private Boolean isSystem;

    @Description("数据类型")
    @Dictionary(value = ReportDataType.class, groups = {WhenAdd.class, WhenUpdate.class})
    @NotNull(groups = {WhenAdd.class, WhenUpdate.class}, message = "数据类型不允许为空")
    @Column(columnDefinition = "int UNSIGNED default 0 comment '数据类型'")
    private Integer dataType;

    /**
     * 设置是否为系统内置参数
     *
     * @param isSystem 是否内置
     * @return 实体
     * @apiNote {@code isSystem} 标了 {@code @ReadOnly}，框架会在绑定请求体时剔除该字段，
     * 所以只能由代码内部（如 {@code SpmsDevData} 初始化）设置
     */
    public ParameterEntity setIsSystem(Boolean isSystem) {
        this.isSystem = isSystem;
        return this;
    }
}
