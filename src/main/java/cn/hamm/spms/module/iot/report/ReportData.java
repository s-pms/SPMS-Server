package cn.hamm.spms.module.iot.report;

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
     * <h2>设备ID,存入后台uuid字段</h2>
     */
    private String deviceId;

    /**
     * <h2>时间戳</h2>
     */
    private Long timestamp;

    /**
     * <h2>负载数据</h2>
     */
    private List<ReportPayload> payloads = new ArrayList<>();

}
