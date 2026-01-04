package cn.hamm.spms.module.iot.report;

import cn.hamm.airpower.web.mqtt.MqttHelper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static cn.hamm.spms.module.iot.report.ReportConstant.IOT_REPORT_TOPIC_V1;

/**
 * <h1>上报事件</h1>
 *
 * @author Hamm.cn
 */
@Component
@Slf4j
public class ReportEventListener {
    @Autowired
    private MqttHelper mqttHelper;

    @Autowired
    private ReportMqCallback reportMqCallback;

    /**
     * 开始监听 MQTT
     *
     * @throws MqttException 异常
     */
    public void listen() throws MqttException {
        try (MqttClient mqttClient = mqttHelper.createClient()) {
            mqttClient.connect(mqttHelper.createOption());
            mqttClient.subscribe(IOT_REPORT_TOPIC_V1);
            mqttClient.setCallback(reportMqCallback);
        }
    }
}
