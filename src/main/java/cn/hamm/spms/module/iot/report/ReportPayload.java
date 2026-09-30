package cn.hamm.spms.module.iot.report;

import cn.hamm.airpower.core.RootModel;
import cn.hamm.airpower.core.annotation.Dictionary;
import cn.hamm.airpower.curd.base.ICurdAction;
import cn.hamm.spms.module.iot.report.enums.ReportGranularity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <h1>数据采集报告负载</h1>
 *
 * @author Hamm.cn
 * @apiNote 该类同时承担两个方向：设备上行时只有 {@code code} / {@code value} 有意义，
 * {@code label} / {@code dataType} 由服务端回查参数表补齐；历史查询时
 * {@code uuid} / {@code reportGranularity} / {@code startTime} / {@code endTime} 才是入参
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ReportPayload extends RootModel<ReportPayload> implements IReportPayloadAction, ICurdAction {
    /**
     * 参数编码，必须是 {@code ParameterEntity} 中已登记的 {@code code}
     */
    @NotBlank(groups = {WhenGetDevicePayloadHistory.class}, message = "参数名不能为空")
    private String code;

    /**
     * 采集值，上行时统一以字符串承载，具体类型由 {@code dataType} 决定
     */
    private String value;

    /**
     * 参数显示名称，服务端回查参数表后补齐，设备不上报
     */
    private String label;

    /**
     * 设备 UUID，即 {@code DeviceEntity#uuid}
     */
    @NotBlank(groups = {WhenGetDevicePayloadHistory.class}, message = "设备采集 ID 不能为空")
    private String uuid;

    /**
     * 历史数据的聚合颗粒度，仅 {@code ReportDataType.NUMBER} 生效
     */
    @NotNull(groups = {WhenGetDevicePayloadHistory.class}, message = "颗粒度不允许为空")
    @Dictionary(value = ReportGranularity.class, groups = {WhenAdd.class, WhenUpdate.class})
    private Integer reportGranularity;

    /**
     * 数据类型，取自 {@code ReportDataType}
     */
    private Integer dataType;

    /**
     * 查询起始时间，单位毫秒
     */
    @NotNull(groups = {WhenGetDevicePayloadHistory.class}, message = "开始时间不允许为空")
    private Long startTime;

    /**
     * 查询结束时间，单位毫秒
     */
    @NotNull(groups = {WhenGetDevicePayloadHistory.class}, message = "结束时间不允许为空")
    private Long endTime;
}
