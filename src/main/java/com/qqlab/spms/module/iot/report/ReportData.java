package com.qqlab.spms.module.iot.report;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * <h1>数据采集报告</h1>
 *
 * @author Hamm
 */
@Data
@Accessors(chain = true)
public class ReportData {
    /**
     * <h2>运行状态</h2>
     */
    public static final String STATUS = "status";

    /**
     * <h2>产量事件</h2>
     */
    public static final String PART_COUNT = "partCount";

    /**
     * <h2>报警事件</h2>
     */
    public static final String ALARM = "alarm";

    /**
     * <h2>设备ID,存入后台uuid字段</h2>
     */
    private String deviceId;

    /**
     * <h2>上报真实时间</h2>
     * <p>
     * 客户端可能断网重发
     */
    private Long timestamp;

    /**
     * <h2>负载数据</h2>
     */
    private List<ReportPayload> payloads = new ArrayList<>();

}
