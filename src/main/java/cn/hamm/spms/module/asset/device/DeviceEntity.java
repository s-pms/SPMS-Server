package cn.hamm.spms.module.asset.device;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.core.annotation.Dictionary;
import cn.hamm.airpower.core.annotation.Meta;
import cn.hamm.airpower.core.annotation.ReadOnly;
import cn.hamm.airpower.web.annotation.Search;
import cn.hamm.spms.base.BaseEntity;
import cn.hamm.spms.common.annotation.AutoGenerateCode;
import cn.hamm.spms.module.asset.device.enums.DeviceAlarm;
import cn.hamm.spms.module.asset.device.enums.DeviceStatus;
import cn.hamm.spms.module.iot.parameter.ParameterEntity;
import cn.hamm.spms.module.iot.report.ReportConstant;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.Set;

import static cn.hamm.spms.module.system.coderule.enums.CodeRuleField.DeviceCode;

/**
 * <h1>设备实体</h1>
 *
 * @author zfy
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Entity
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "device")
@Description("设备")
public class DeviceEntity extends BaseEntity<DeviceEntity> implements IDeviceAction {
    @Description("设备名称")
    @Search
    @Column(columnDefinition = "varchar(255) default '' comment '设备名称'", unique = true)
    @NotBlank(groups = {WhenAdd.class, WhenUpdate.class}, message = "设备名称不能为空")
    @Meta
    private String name;

    @Description("设备编码")
    @Column(columnDefinition = "varchar(255) default '' comment '设备编码'", unique = true)
    @AutoGenerateCode(DeviceCode)
    @Meta
    private String code;

    @Description("设备 UUID")
    @Search
    @Column(columnDefinition = "varchar(255) default '' comment '设备UUID'", unique = true)
    @NotBlank(groups = {WhenGetDeviceConfig.class}, message = "请传入设备uuid(采集端deviceId)")
    private String uuid;

    @Description("设备状态")
    @ReadOnly
    @Column(columnDefinition = "int UNSIGNED default 4 comment '设备状态'")
    @Dictionary(value = DeviceStatus.class, groups = {WhenAdd.class, WhenUpdate.class})
    private Integer status;

    @Description("报警状态")
    @ReadOnly
    @Column(columnDefinition = "int UNSIGNED default 0 comment '报警状态'")
    @Dictionary(value = DeviceAlarm.class, groups = {WhenAdd.class, WhenUpdate.class})
    private Integer alarm;

    @Description("实时产量")
    @ReadOnly
    @Column(columnDefinition = "bigint UNSIGNED default 0 comment '实时产量'")
    private Long partCount;

    @Description("开启采集")
    @Column(columnDefinition = "bit(1) default 1 comment '开启采集'")
    private Boolean isReporting;

    @Description("采集频率")
    @Column(columnDefinition = "int UNSIGNED default 1000 comment '采集频率'")
    @NotNull(groups = {WhenUpdate.class, WhenAdd.class}, message = "采集频率不能为空")
    @Min(ReportConstant.REPORT_RATE_MIN)
    private Integer rate;

    @Description("参数列表")
    @ManyToMany(fetch = FetchType.LAZY)
    private Set<ParameterEntity> parameters;
}
