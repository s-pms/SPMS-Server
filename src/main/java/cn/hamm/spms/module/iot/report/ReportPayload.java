package cn.hamm.spms.module.iot.report;

import cn.hamm.airpower.interfaces.IAction;
import cn.hamm.airpower.interfaces.IEntityAction;
import cn.hamm.airpower.root.RootModel;
import cn.hamm.airpower.validate.dictionary.Dictionary;
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
public class ReportPayload extends RootModel implements IReportPayloadAction, IEntityAction, IAction {
    /**
     * <h2>属性名</h2>
     */
    @NotBlank(groups = {WhenGetDevicePayloadHistory.class}, message = "参数名不能为空")
    private String code;

    /**
     * <h2>属性值</h2>
     */
    private String value;

    /**
     * <h2>显示的名称</h2>
     */
    private String label;

    /**
     * <h2>时序存储设备ID</h2>
     */
    @NotBlank(groups = {WhenGetDevicePayloadHistory.class}, message = "设备采集ID不能为空")
    private String uuid;

    /**
     * <h2>颗粒度不允许为空</h2>
     */
    @NotNull(groups = {WhenGetDevicePayloadHistory.class}, message = "颗粒度不允许为空")
    @Dictionary(value = ReportGranularity.class, groups = {WhenAdd.class, WhenUpdate.class})
    private Integer reportGranularity;

    /**
     * <h2>数据类型</h2>
     */
    private Integer dataType;

    /**
     * <h2>开始时间</h2>
     */
    @NotNull(groups = {WhenGetDevicePayloadHistory.class}, message = "开始时间不允许为空")
    private Long startTime;

    /**
     * <h2>结束时间</h2>
     */
    @NotNull(groups = {WhenGetDevicePayloadHistory.class}, message = "结束时间不允许为空")
    private Long endTime;
}
