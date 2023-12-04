package com.qqlab.spms.module.iot.report;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
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
    private String uuid;


    /**
     * <h2>值的布尔值</h2>
     *
     * @return 布尔值
     */
    public boolean parseBoolValue() {
        return Integer.parseInt(value) == 1;
    }

    /**
     * <h2>值的数值</h2>
     *
     * @return 数值
     */
    public double parseNumberValue() {
        return Double.parseDouble(value);
    }
}
