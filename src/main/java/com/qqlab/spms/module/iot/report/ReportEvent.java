package com.qqlab.spms.module.iot.report;

import cn.hamm.airpower.mqtt.MqttHelper;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qqlab.spms.module.asset.device.DeviceEntity;
import com.qqlab.spms.module.asset.device.DeviceService;
import com.qqlab.spms.module.iot.collection.CollectionEntity;
import com.qqlab.spms.module.iot.collection.CollectionService;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * <h1>上报事件</h1>
 *
 * @author Hamm
 */
@Component
public class ReportEvent {
    @Autowired
    private MqttHelper mqttHelper;

    /**
     * <h2>订阅Topic</h2>
     */
    public final static String IOT_REPORT_TOPIC_V1 = "/sys/msg/v1";

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private CollectionService collectionService;

    /**
     * <h2>开始监听MQTT</h2>
     *
     * @throws MqttException 异常
     */
    public void listen() throws MqttException {
        MqttClient mqttClient = mqttHelper.createClient();
        mqttClient.connect(mqttHelper.createOption());
        mqttClient.subscribe(ReportEvent.IOT_REPORT_TOPIC_V1);
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws JsonProcessingException {
                String reportString = new String(mqttMessage.getPayload());
                System.out.println(reportString);
                ObjectMapper mapper = new ObjectMapper();
                mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
                ReportData reportData = mapper.readValue(reportString, ReportData.class);
                DeviceEntity device = deviceService.getByUuid(reportData.getDeviceId());
                if (Objects.isNull(device)) {
                    return;
                }
                for (ReportPayload payload : reportData.getPayloads()) {
                    if (Objects.isNull(payload.getValue())) {
                        return;
                    }
                    switch (payload.getCode()) {
                        case ReportData.STATUS:
                            device.setStatus(Integer.parseInt(payload.getValue()));
                            deviceService.update(device);
                            break;
                        case ReportData.ALARM:
                            device.setAlarm(Integer.parseInt(payload.getValue()));
                            deviceService.update(device);
                            break;
                        case ReportData.PART_COUNT:
                            device.setPartCount(Long.parseLong(payload.getValue()));
                            deviceService.update(device);
                            break;
                        default:
                    }
                    CollectionEntity last = collectionService.getDistinctFirstByUuidAndCode(reportData.getDeviceId(), payload.getCode());
                    if (Objects.nonNull(last) && last.getValue().equals(payload.getValue())) {
                        continue;
                    }
                    collectionService.add(new CollectionEntity()
                            .setCode(payload.getCode())
                            .setValue(payload.getValue())
                            .setUuid(reportData.getDeviceId())
                            .setTimestamp(reportData.getTimestamp())
                    );
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }
}
