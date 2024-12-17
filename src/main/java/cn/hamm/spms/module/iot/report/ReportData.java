package cn.hamm.spms.module.iot.report;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * <h1>数据采集报告</h1>
 *
 * @author Hamm.cn
 */
@Data
@Accessors(chain = true)
public class ReportData {
    /**
     * <h3>设备ID,存入后台uuid字段</h3>
     */
    private String deviceId;

    /**
     * <h3>时间戳</h3>
     */
    private Long timestamp;

    /**
     * <h3>负载数据</h3>
     */
    private List<ReportPayload> payloads = new ArrayList<>();

}
