package cn.hamm.spms.module.iot.report;

import cn.hamm.airpower.core.RootModel;
import cn.hamm.airpower.core.annotation.Dictionary;
import cn.hamm.airpower.curd.ICurdAction;
import cn.hamm.spms.module.iot.report.enums.ReportGranularity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <h1>数据采集报告</h1>
 *
 * @author Hamm.cn
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ReportPayload extends RootModel<ReportPayload> implements IReportPayloadAction, ICurdAction {
    /**
     * 属性名
     */
    @NotBlank(groups = {WhenGetDevicePayloadHistory.class}, message = "参数名不能为空")
    private String code;

    /**
     * 属性值
     */
    private String value;

    /**
     * 显示的名称
     */
    private String label;

    /**
     * 时序存储设备 ID
     */
    @NotBlank(groups = {WhenGetDevicePayloadHistory.class}, message = "设备采集 ID 不能为空")
    private String uuid;

    /**
     * 颗粒度不允许为空
     */
    @NotNull(groups = {WhenGetDevicePayloadHistory.class}, message = "颗粒度不允许为空")
    @Dictionary(value = ReportGranularity.class, groups = {WhenAdd.class, WhenUpdate.class})
    private Integer reportGranularity;

    /**
     * 数据类型
     */
    private Integer dataType;

    /**
     * 开始时间
     */
    @NotNull(groups = {WhenGetDevicePayloadHistory.class}, message = "开始时间不允许为空")
    private Long startTime;

    /**
     * 结束时间
     */
    @NotNull(groups = {WhenGetDevicePayloadHistory.class}, message = "结束时间不允许为空")
    private Long endTime;
}
