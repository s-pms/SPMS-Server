package cn.hamm.spms.module.iot.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import lombok.Data;
import lombok.experimental.Accessors;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * <h1>上报数据时序存储记录</h1>
 *
 * @author Hamm.cn
 * @apiNote 四个类型字段各自对应一种 {@code ReportDataType}，由查询时按类型填充；
 * InfluxDB 侧只有 {@code value} 打了 {@code @Column}，其余三个不是真实的 field
 */
@Data
@Accessors(chain = true)
@Measurement(name = "report")
@JsonInclude(NON_NULL)
public class ReportInfluxPayload {
    /**
     * 数值型采集值，InfluxDB 的 field 名就是 {@code value}
     */
    @Column
    private Double value;

    /**
     * 设备 UUID，InfluxDB 的 tag，索引靠它
     */
    @Column(tag = true)
    private String uuid;

    /**
     * 采集时间戳
     */
    private Long timestamp;

    /**
     * 布尔型采集值
     */
    private Boolean boolValue;

    /**
     * 整型采集值
     */
    private Integer intValue;

    /**
     * 字符串型采集值
     */
    private String strValue;
}
