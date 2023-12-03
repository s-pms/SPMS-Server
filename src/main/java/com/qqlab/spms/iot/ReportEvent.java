package com.qqlab.spms.iot;

import cn.hamm.airpower.mqtt.MqttHelper;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qqlab.spms.iot.influxdb.InfluxHelper;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <h1>上报事件</h1>
 *
 * @author Hamm
 */
@Component
public class ReportEvent {
    @Autowired
    private MqttHelper mqttHelper;

    @Autowired(required = false)
    private InfluxHelper influxHelper;

    /**
     * <h2>订阅Topic</h2>
     */
    public final static String IOT_REPORT_TOPIC = "IOT/";
    private final String IOT_REPORT_STATUS = "STATUS";
    private final String IOT_REPORT_ALARM = "ALARM";
    private final String IOT_REPORT_PARTCOUNT = "PARTCOUNT";

    public void listen() throws MqttException {
        MqttClient mqttClient = mqttHelper.createClient();
        mqttClient.connect(mqttHelper.createOption());
        String[] topics = {
                ReportEvent.IOT_REPORT_TOPIC + IOT_REPORT_STATUS,
                ReportEvent.IOT_REPORT_TOPIC + IOT_REPORT_ALARM,
                ReportEvent.IOT_REPORT_TOPIC + IOT_REPORT_PARTCOUNT,
        };
        mqttClient.subscribe(topics);
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws JsonProcessingException {
                String topicType = topic.replaceAll(ReportEvent.IOT_REPORT_TOPIC, "");
                String reportString = new String(mqttMessage.getPayload());
                System.out.println(reportString);
                switch (topicType) {
                    case IOT_REPORT_STATUS:
                        break;
                    case IOT_REPORT_ALARM:
                        break;
                    case IOT_REPORT_PARTCOUNT:
                        break;
                    default:
                        return;
                }
                ObjectMapper mapper = new ObjectMapper();
                mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
                ReportData reportData = mapper.readValue(reportString, ReportData.class);
                influxHelper.save(reportData);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }
}
