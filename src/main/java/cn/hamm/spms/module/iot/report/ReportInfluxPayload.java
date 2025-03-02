package cn.hamm.spms.module.iot.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import lombok.Data;
import lombok.experimental.Accessors;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * <h1>数据采集时序存储报告</h1>
 *
 * @author Hamm.cn
 */
@Data
@Accessors(chain = true)
@Measurement(name = "report")
@JsonInclude(NON_NULL)
public class ReportInfluxPayload {
    /**
     * <h3>属性值</h3>
     */
    @Column
    private Double value;

    /**
     * <h3>时序存储设备ID</h3>
     */
    @Column(tag = true)
    private String uuid;

    /**
     * <h3>时间戳</h3>
     */
    private Long timestamp;

    /**
     * <h3>布尔值</h3>
     */
    private Boolean boolValue;

    /**
     * <h3>整形</h3>
     */
    private Integer intValue;

    /**
     * <h3>字符串</h3>
     */
    private String strValue;
}
