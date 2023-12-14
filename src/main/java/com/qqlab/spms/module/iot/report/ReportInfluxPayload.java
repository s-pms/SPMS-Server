package com.qqlab.spms.module.iot.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <h1>数据采集时序存储报告</h1>
 *
 * @author Hamm
 */
@Data
@Accessors(chain = true)
@Measurement(name = "report")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportInfluxPayload {
    /**
     * 属性名
     */
    @Column(tag = true)
    private String code;

    /**
     * 属性值
     */
    @Column
    private Double value;

    /**
     * 时序存储设备ID
     */
    @Column
    private String uuid;

    private Long timestamp;
}
