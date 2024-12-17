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
public class ReportPayload extends RootModel<ReportPayload> implements IReportPayloadAction, IEntityAction, IAction {
    /**
     * <h3>属性名</h3>
     */
    @NotBlank(groups = {WhenGetDevicePayloadHistory.class}, message = "参数名不能为空")
    private String code;

    /**
     * <h3>属性值</h3>
     */
    private String value;

    /**
     * <h3>显示的名称</h3>
     */
    private String label;

    /**
     * <h3>时序存储设备ID</h3>
     */
    @NotBlank(groups = {WhenGetDevicePayloadHistory.class}, message = "设备采集ID不能为空")
    private String uuid;

    /**
     * <h3>颗粒度不允许为空</h3>
     */
    @NotNull(groups = {WhenGetDevicePayloadHistory.class}, message = "颗粒度不允许为空")
    @Dictionary(value = ReportGranularity.class, groups = {WhenAdd.class, WhenUpdate.class})
    private Integer reportGranularity;

    /**
     * <h3>数据类型</h3>
     */
    private Integer dataType;

    /**
     * <h3>开始时间</h3>
     */
    @NotNull(groups = {WhenGetDevicePayloadHistory.class}, message = "开始时间不允许为空")
    private Long startTime;

    /**
     * <h3>结束时间</h3>
     */
    @NotNull(groups = {WhenGetDevicePayloadHistory.class}, message = "结束时间不允许为空")
    private Long endTime;
}
