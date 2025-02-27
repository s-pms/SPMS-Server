package cn.hamm.spms.module.iot.report;


import cn.hamm.airpower.helper.MqttHelper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
     * <h3>开始监听MQTT</h3>
     *
     * @throws MqttException 异常
     * @noinspection AlibabaMethodTooLong
     */
    public void listen() throws MqttException {
        try (MqttClient mqttClient = mqttHelper.createClient()) {
            mqttClient.connect(mqttHelper.createOption());
            mqttClient.subscribe(ReportConstant.IOT_REPORT_TOPIC_V1);
            mqttClient.setCallback(reportMqCallback);
        }
    }
}
