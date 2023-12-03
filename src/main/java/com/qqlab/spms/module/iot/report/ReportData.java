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
@Measurement(name = "report")
public class ReportData {
    /**
     * <h2>唯一ID</h2>
     */
    @Column(tag = true)
    private String uuid;

    /**
     * <h2>采集类型</h2>
     * <li>0. 自定义</li>
     * <li>1. 运行状态</li>
     * <li>2. 报警状态</li>
     * <li>3. 实时产量</li>
     */
    @Column
    private ReportType type;

    /**
     * 自定义编码
     */
    @Column
    private String code;

    /**
     * 自定义值
     */
    @Column
    private String value;


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
