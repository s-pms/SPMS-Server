package com.qqlab.spms.module.iot.report;

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
     * 自定义编码
     */
    private String code;

    /**
     * 自定义值
     */
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
