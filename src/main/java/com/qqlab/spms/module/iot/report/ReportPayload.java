package com.qqlab.spms.module.iot.report;

import cn.hamm.airpower.validate.dictionary.Dictionary;
import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
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
@Measurement(name = "payload")
public class ReportPayload {
    /**
     * 属性名
     */
    @Column(tag = true)
    @NotBlank(groups = {WhenGetDevicePayloadHistory.class}, message = "参数名不能为空")
    private String code;

    /**
     * 属性值
     */
    @Column
    private String value;

    /**
     * 显示的名称
     */
    @Column
    private String label;

    /**
     * 时序存储设备ID
     */
    @Column
    @NotBlank(groups = {WhenGetDevicePayloadHistory.class}, message = "设备采集ID不能为空")
    private String uuid;

    @NotNull(groups = {WhenGetDevicePayloadHistory.class}, message = "粒度不能为空")
    @Dictionary(ReportGranularity.class)
    private int granularity;


    /**
     * 值的布尔值
     *
     * @return 布尔值
     */
    public boolean parseBoolValue() {
        return Integer.parseInt(value) == 1;
    }

    /**
     * 值的数值
     *
     * @return 数值
     */
    public double parseNumberValue() {
        return Double.parseDouble(value);
    }

    public interface WhenGetDevicePayloadHistory {
    }
}
