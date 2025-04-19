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
     * 设备ID,存入后台uuid字段
     */
    private String deviceId;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 负载数据
     */
    private List<ReportPayload> payloads = new ArrayList<>();

}
