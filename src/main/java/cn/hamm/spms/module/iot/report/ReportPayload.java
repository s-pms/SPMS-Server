package cn.hamm.spms.module.iot.report;

import cn.hamm.airpower.root.RootEntity;
import cn.hamm.airpower.validate.dictionary.Dictionary;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <h1>数据采集报告</h1>
 *
 * @author Hamm
 */
@Data
@Accessors(chain = true)
public class ReportPayload {
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
     * 时序存储设备ID
     */
    @NotBlank(groups = {WhenGetDevicePayloadHistory.class}, message = "设备采集ID不能为空")
    private String uuid;

    @NotNull(groups = {WhenGetDevicePayloadHistory.class}, message = "颗粒度不允许为空")
    @Dictionary(value = ReportGranularity.class,groups = {RootEntity.WhenAdd.class, RootEntity.WhenUpdate.class})
    private Integer reportGranularity;

    private Integer dataType;


    @NotNull(groups = {WhenGetDevicePayloadHistory.class}, message = "开始时间不允许为空")
    private Long startTime;

    @NotNull(groups = {WhenGetDevicePayloadHistory.class}, message = "结束时间不允许为空")
    private Long endTime;

    public interface WhenGetDevicePayloadHistory {
    }
}
