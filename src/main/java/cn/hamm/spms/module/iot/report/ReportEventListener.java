package cn.hamm.spms.module.iot.report;

import cn.hamm.airpower.mqtt.MqttHelper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static cn.hamm.spms.module.iot.report.ReportConstant.IOT_REPORT_TOPIC_V1;

/**
 * <h1>设备数据上报监听</h1>
 *
 * @author Hamm.cn
 * @apiNote {@code listen} 是 {@code try-with-resources}，方法返回即断开会话，连接是临时的
 */
@Component
@Slf4j
public class ReportEventListener {
    @Autowired
    private MqttHelper mqttHelper;

    @Autowired
    private ReportMqCallback reportMqCallback;

    /**
     * 订阅设备上报 Topic
     *
     * @throws MqttException 连接或订阅失败时抛出
     */
    public void listen() throws MqttException {
        try (MqttClient mqttClient = mqttHelper.createClient()) {
            mqttClient.connect(mqttHelper.createOption());
            mqttClient.subscribe(IOT_REPORT_TOPIC_V1);
            mqttClient.setCallback(reportMqCallback);
        }
    }
}
