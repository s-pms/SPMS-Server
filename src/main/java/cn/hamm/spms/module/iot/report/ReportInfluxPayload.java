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
     * 属性值
     */
    @Column
    private Double value;

    /**
     * 时序存储设备 ID
     */
    @Column(tag = true)
    private String uuid;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 布尔值
     */
    private Boolean boolValue;

    /**
     * 整形
     */
    private Integer intValue;

    /**
     * 字符串
     */
    private String strValue;
}
