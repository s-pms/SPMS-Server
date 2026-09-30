package cn.hamm.spms.module.iot.report;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * <h1>设备数据上报报文</h1>
 *
 * @author Hamm.cn
 * @apiNote 即 MQTT 报文 {@code sys/msg/v1} 的 JSON 结构，设备端产出、服务端消费后会把
 * {@code payloads} 换成已匹配到参数定义并去重后的结果再缓存
 */
@Data
@Accessors(chain = true)
public class ReportData {
    /**
     * 设备 ID，对应库中设备的 {@code uuid} 字段
     */
    private String deviceId;

    /**
     * 设备侧产生该报文的时间戳，单位毫秒
     */
    private Long timestamp;

    /**
     * 本次上报携带的采集值列表
     */
    private List<ReportPayload> payloads = new ArrayList<>();

}
