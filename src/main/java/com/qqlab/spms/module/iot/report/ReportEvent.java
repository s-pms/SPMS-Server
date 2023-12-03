package com.qqlab.spms.module.iot.report;

import cn.hamm.airpower.mqtt.MqttHelper;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qqlab.spms.helper.influxdb.InfluxHelper;
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

    @Autowired(required = false)
    private InfluxHelper influxHelper;

    /**
     * <h2>订阅Topic</h2>
     */
    public final static String IOT_REPORT_TOPIC = "IOT";
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
        mqttClient.subscribe(ReportEvent.IOT_REPORT_TOPIC);
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
//                influxHelper.save(reportData);
                DeviceEntity device;
                if (Objects.isNull(reportData.getValue())) {
                    return;
                }
                switch (reportData.getType()) {
                    case STATUS:
                        device = deviceService.getByUuid(reportData.getUuid());
                        if (Objects.isNull(device)) {
                            return;
                        }
                        device.setStatus(Integer.parseInt(reportData.getValue()));
                        deviceService.update(device);
                        reportData.setCode("status");
                        break;
                    case ALARM:
                        device = deviceService.getByUuid(reportData.getUuid());
                        if (Objects.isNull(device)) {
                            return;
                        }
                        device.setAlarm(Integer.parseInt(reportData.getValue()));
                        deviceService.update(device);
                        reportData.setCode("alarm");
                        break;
                    case PART_COUNT:
                        device = deviceService.getByUuid(reportData.getUuid());
                        if (Objects.isNull(device)) {
                            return;
                        }
                        device.setPartCount(Long.parseLong(reportData.getValue()));
                        deviceService.update(device);
                        reportData.setCode("partCount");
                        break;
                    default:
                }
                collectionService.add(new CollectionEntity()
                        .setCode(reportData.getCode())
                        .setValue(reportData.getValue())
                        .setUuid(reportData.getUuid())
                );
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }
}
