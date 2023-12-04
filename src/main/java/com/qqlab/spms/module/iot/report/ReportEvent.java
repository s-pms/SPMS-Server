package com.qqlab.spms.module.iot.report;

import cn.hamm.airpower.mqtt.MqttHelper;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qqlab.spms.helper.influxdb.InfluxHelper;
import com.qqlab.spms.module.asset.device.DeviceEntity;
import com.qqlab.spms.module.asset.device.DeviceService;
import com.qqlab.spms.module.iot.parameter.ParameterEntity;
import com.qqlab.spms.module.iot.parameter.ParameterService;
import jakarta.annotation.Resource;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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

    /**
     * <h2>Redis存IOT采集数据的前缀</h2>
     */
    public final static String PREFIX = "iot_";
    /**
     * 下划线
     */
    public final static String UNDERLINE = "_";
    /**
     * 报告缓存时长
     */
    private final int REPORT_CACHE_SECOND = 30;
    @Autowired
    private ParameterService parameterService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private InfluxHelper influxHelper;


    /**
     * <h2>开始监听MQTT</h2>
     *
     * @throws MqttException 异常
     * @noinspection AlibabaMethodTooLong
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
            public void messageArrived(String topic, MqttMessage mqttMessage) {
                String reportString = new String(mqttMessage.getPayload());
                System.out.println(reportString);
                ObjectMapper mapper = new ObjectMapper();
                mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
                try {
                    ReportData reportData = mapper.readValue(reportString, ReportData.class);
                    DeviceEntity device = null;
                    String lastDataInCache;
                    List<ReportPayload> payloadList = new ArrayList<>();
                    for (ReportPayload payload : reportData.getPayloads()) {
                        if (Objects.isNull(payload.getValue())) {
                            continue;
                        }
                        ParameterEntity parameterEntity = parameterService.getByCode(payload.getCode());
                        if (Objects.isNull(parameterEntity)) {
                            continue;
                        }
                        payload.setUuid(reportData.getDeviceId());
                        ReportPayload newPayload = new ReportPayload().setCode(payload.getCode()).setValue(payload.getValue()).setLabel(parameterEntity.getLabel());
                        payloadList.add(newPayload);
                        payload.setUuid(reportData.getDeviceId());
                        //noinspection EnhancedSwitchMigration
                        switch (payload.getCode()) {
                            case ReportData.STATUS:
                                lastDataInCache = (String) redisTemplate.opsForValue().get(PREFIX + ReportData.STATUS + UNDERLINE + reportData.getDeviceId());
                                if (Objects.nonNull(lastDataInCache) && lastDataInCache.equals(payload.getValue())) {
                                    // 查到了数据 没过期 跳过
                                    continue;
                                }
                                redisTemplate.opsForValue().set(PREFIX + ReportData.STATUS + UNDERLINE + reportData.getDeviceId(), payload
                                        .getValue(), REPORT_CACHE_SECOND, TimeUnit.SECONDS);
                                if (Objects.isNull(device)) {
                                    device = deviceService.getByUuid(reportData.getDeviceId());
                                    if (Objects.nonNull(device)) {
                                        device.setStatus(Integer.parseInt(payload.getValue()));
                                        deviceService.update(device);
                                    }
                                }
                                break;
                            case ReportData.ALARM:
                                lastDataInCache = (String) redisTemplate.opsForValue().get(PREFIX + ReportData.ALARM + UNDERLINE + reportData.getDeviceId());
                                if (Objects.nonNull(lastDataInCache) && lastDataInCache.equals(payload.getValue())) {
                                    // 查到了数据 没过期 跳过
                                    continue;
                                }
                                redisTemplate.opsForValue().set(PREFIX + ReportData.ALARM + UNDERLINE + reportData.getDeviceId(), payload
                                        .getValue(), REPORT_CACHE_SECOND, TimeUnit.SECONDS);
                                if (Objects.isNull(device)) {
                                    device = deviceService.getByUuid(reportData.getDeviceId());
                                    if (Objects.nonNull(device)) {
                                        device.setStatus(Integer.parseInt(payload.getValue()));
                                        deviceService.update(device);
                                    }
                                }
                                break;
                            case ReportData.PART_COUNT:
                                lastDataInCache = (String) redisTemplate.opsForValue().get(PREFIX + ReportData.PART_COUNT + UNDERLINE + reportData.getDeviceId());
                                if (Objects.nonNull(lastDataInCache) && lastDataInCache.equals(payload.getValue())) {
                                    // 查到了数据 没过期 跳过
                                    continue;
                                }
                                redisTemplate.opsForValue().set(PREFIX + ReportData.PART_COUNT + UNDERLINE + reportData.getDeviceId(), payload
                                        .getValue(), REPORT_CACHE_SECOND, TimeUnit.SECONDS);
                                if (Objects.isNull(device)) {
                                    device = deviceService.getByUuid(reportData.getDeviceId());
                                    if (Objects.nonNull(device)) {
                                        device.setPartCount(Long.parseLong(payload.getValue()));
                                        deviceService.update(device);
                                    }
                                }
                                break;
                            default:
                                lastDataInCache = (String) redisTemplate.opsForValue().get(PREFIX + payload.getCode() + UNDERLINE + reportData.getDeviceId());
                                if (Objects.nonNull(lastDataInCache) && lastDataInCache.equals(payload.getValue())) {
                                    // 查到了数据 没过期 跳过
                                    continue;
                                }
                                redisTemplate.opsForValue().set(PREFIX + payload.getCode() + UNDERLINE + reportData.getDeviceId(), payload
                                        .getValue(), REPORT_CACHE_SECOND, TimeUnit.SECONDS);
                        }
                        influxHelper.save(payload);
                    }
                    reportData.setPayloads(payloadList);
                    redisTemplate.opsForValue().set(PREFIX + reportData.getDeviceId(), JSON.toJSONString(reportData));
                } catch (Exception ignored) {

                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }
}
