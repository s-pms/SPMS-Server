package com.qqlab.spms.iot;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import com.qqlab.spms.iot.payload.PayloadData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

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
     * <h2>采集类型</h2>
     */
    @Column
    private Integer type;

    /**
     * <h2>唯一ID</h2>
     */
    @Column(tag = true)
    private String uuid;

    /**
     * <h2>毫秒时间戳</h2>
     */
    @Column
    private Long time;

    /**
     * <h2>运行状态</h2>
     */
    @Column
    private Integer status;

    /**
     * <h2>报警状态</h2>
     */
    @Column
    private Integer alarm;

    /**
     * <h2>实时产量</h2>
     */
    @Column
    private Long partCount;

    /**
     * <h2>其他负载</h2>
     */
    private List<PayloadData> payloads;
}
