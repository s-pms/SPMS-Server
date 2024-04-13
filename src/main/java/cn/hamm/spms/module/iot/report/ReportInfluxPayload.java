package cn.hamm.spms.module.iot.report;

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
     * <h2>属性值</h2>
     */
    @Column
    private Double value;

    /**
     * <h2>时序存储设备ID</h2>
     */
    @Column(tag = true)
    private String uuid;

    /**
     * <h2>时间戳</h2>
     */
    private Long timestamp;

    /**
     * <h2>布尔值</h2>
     */
    private Boolean boolValue;

    /**
     * <h2>整形</h2>
     */
    private Integer intValue;

    /**
     * <h2>字符串</h2>
     */
    private String strValue;
}
