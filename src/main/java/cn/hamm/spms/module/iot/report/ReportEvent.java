package cn.hamm.spms.module.iot.report;


import cn.hamm.airpower.config.Constant;
import cn.hamm.airpower.model.Json;
import cn.hamm.airpower.util.Utils;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.common.helper.influxdb.InfluxHelper;
import cn.hamm.spms.module.asset.device.DeviceEntity;
import cn.hamm.spms.module.iot.parameter.ParameterEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <h1>上报事件</h1>
 *
 * @author Hamm.cn
 */
@Component
@Slf4j
public class ReportEvent {
    /**
     * <h2>数据上报毫秒最小</h2>
     */
    public static final int REPORT_RATE_MIN = 200;

    /**
     * <h2>运行状态</h2>
     */
    public static final String REPORT_KEY_OF_STATUS = "Status";

    /**
     * <h2>产量事件</h2>
     */
    public static final String REPORT_KEY_OF_PART_COUNT = "PartCnt";

    /**
     * <h2>报警事件</h2>
     */
    public static final String REPORT_KEY_OF_ALARM = "Alarm";

    /**
     * <h2>订阅Topic</h2>
     */
    public final static String IOT_REPORT_TOPIC_V1 = "sys/msg/v1";

    /**
     * <h2>Redis存IOT采集数据的前缀</h2>
     */
    public final static String CACHE_PREFIX = "iot_report_";

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
        try (MqttClient mqttClient = Utils.getMqttUtil().createClient()) {
            mqttClient.connect(Utils.getMqttUtil().createOption());
            mqttClient.subscribe(ReportEvent.IOT_REPORT_TOPIC_V1);
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                }

                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage) {
                    String reportString = new String(mqttMessage.getPayload());
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
                    try {
                        ReportData reportData = mapper.readValue(reportString, ReportData.class);
                        DeviceEntity device = null;
                        String lastDataInCache;
                        List<ReportPayload> payloadList = new ArrayList<>();
                        if (Objects.isNull(reportData.getPayloads())) {
                            return;
                        }
                        for (ReportPayload payload : reportData.getPayloads()) {
                            if (Objects.isNull(payload.getValue())) {
                                continue;
                            }
                            ParameterEntity parameterEntity = Services.getParameterService().getByCode(payload.getCode());
                            if (Objects.isNull(parameterEntity)) {
                                continue;
                            }
                            payload.setUuid(reportData.getDeviceId());
                            ReportPayload newPayload = new ReportPayload()
                                    .setCode(payload.getCode())
                                    .setValue(payload.getValue())
                                    .setLabel(parameterEntity.getLabel())
                                    .setDataType(parameterEntity.getDataType());
                            payloadList.add(newPayload);
                            payload.setUuid(reportData.getDeviceId());
                            switch (payload.getCode()) {
                                case REPORT_KEY_OF_STATUS:
                                    lastDataInCache = (String) redisTemplate.opsForValue().get(CACHE_PREFIX + REPORT_KEY_OF_STATUS + Constant.UNDERLINE + reportData.getDeviceId());
                                    if (Objects.nonNull(lastDataInCache) && lastDataInCache.equals(payload.getValue())) {
                                        // 查到了数据 没过期 跳过
                                        continue;
                                    }
                                    influxHelper.save(payload.getCode(), Integer.parseInt(payload.getValue()), reportData.getDeviceId());
                                    redisTemplate.opsForValue().set(CACHE_PREFIX + REPORT_KEY_OF_STATUS + Constant.UNDERLINE + reportData.getDeviceId(), payload
                                            .getValue());
                                    if (Objects.isNull(device)) {
                                        device = Services.getDeviceService().getByUuid(reportData.getDeviceId());
                                        if (Objects.nonNull(device) && device.getIsReporting()) {
                                            device.setStatus(Integer.parseInt(payload.getValue()));
                                            Services.getDeviceService().update(device);
                                        }
                                    }
                                    break;
                                case REPORT_KEY_OF_ALARM:
                                    lastDataInCache = (String) redisTemplate.opsForValue().get(CACHE_PREFIX + REPORT_KEY_OF_ALARM + Constant.UNDERLINE + reportData.getDeviceId());
                                    if (Objects.nonNull(lastDataInCache) && lastDataInCache.equals(payload.getValue())) {
                                        // 查到了数据 没过期 跳过
                                        continue;
                                    }
                                    influxHelper.save(payload.getCode(), Integer.parseInt(payload.getValue()), reportData.getDeviceId());
                                    redisTemplate.opsForValue().set(CACHE_PREFIX + REPORT_KEY_OF_ALARM + Constant.UNDERLINE + reportData.getDeviceId(), payload
                                            .getValue());
                                    if (Objects.isNull(device)) {
                                        device = Services.getDeviceService().getByUuid(reportData.getDeviceId());
                                        if (Objects.nonNull(device) && device.getIsReporting()) {
                                            device.setAlarm(Integer.parseInt(payload.getValue()));
                                            Services.getDeviceService().update(device);
                                        }
                                    }
                                    break;
                                case REPORT_KEY_OF_PART_COUNT:
                                    lastDataInCache = (String) redisTemplate.opsForValue().get(CACHE_PREFIX + REPORT_KEY_OF_PART_COUNT + Constant.UNDERLINE + reportData.getDeviceId());
                                    if (Objects.nonNull(lastDataInCache) && lastDataInCache.equals(payload.getValue())) {
                                        // 查到了数据 没过期 跳过
                                        continue;
                                    }
                                    influxHelper.save(payload.getCode(), Double.parseDouble(payload.getValue()), reportData.getDeviceId());
                                    redisTemplate.opsForValue().set(CACHE_PREFIX + REPORT_KEY_OF_PART_COUNT + Constant.UNDERLINE + reportData.getDeviceId(), payload
                                            .getValue());
                                    if (Objects.isNull(device)) {
                                        device = Services.getDeviceService().getByUuid(reportData.getDeviceId());
                                        if (Objects.nonNull(device) && device.getIsReporting()) {
                                            device.setPartCount(Long.parseLong(payload.getValue()));
                                            Services.getDeviceService().update(device);
                                        }
                                    }
                                    break;
                                default:
                                    influxHelper.save(payload.getCode(), payload.getValue(), reportData.getDeviceId());
                                    lastDataInCache = (String) redisTemplate.opsForValue().get(CACHE_PREFIX + payload.getCode() + Constant.UNDERLINE + reportData.getDeviceId());
                                    if (Objects.nonNull(lastDataInCache) && lastDataInCache.equals(payload.getValue())) {
                                        // 查到了数据 没过期 跳过
                                        continue;
                                    }
                                    redisTemplate.opsForValue().set(CACHE_PREFIX + payload.getCode() + Constant.UNDERLINE + reportData.getDeviceId(), payload
                                            .getValue());
                            }
                        }
                        reportData.setPayloads(payloadList);
                        redisTemplate.opsForValue().set(CACHE_PREFIX + reportData.getDeviceId(), Json.toString(reportData));
                    } catch (java.lang.Exception e) {
                        log.error(e.getMessage());
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

                }
            });
        }
    }
}
