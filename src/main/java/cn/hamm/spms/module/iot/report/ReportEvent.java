package cn.hamm.spms.module.iot.report;

import cn.hamm.airpower.mqtt.MqttHelper;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import cn.hamm.spms.common.helper.influxdb.InfluxHelper;
import cn.hamm.spms.module.asset.device.DeviceEntity;
import cn.hamm.spms.module.asset.device.DeviceService;
import cn.hamm.spms.module.iot.parameter.ParameterEntity;
import cn.hamm.spms.module.iot.parameter.ParameterService;
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
 * @author Hamm
 */
@Component
@Slf4j
public class ReportEvent {
    /**
     * 数据上报毫秒最小
     */
    public static final int REPORT_RATE_MIN = 200;
    /**
     * 运行状态
     */
    public static final String REPORT_KEY_OF_STATUS = "Status";

    /**
     * 产量事件
     */
    public static final String REPORT_KEY_OF_PART_COUNT = "PartCnt";

    /**
     * 报警事件
     */
    public static final String REPORT_KEY_OF_ALARM = "Alarm";

    /**
     * 订阅Topic
     */
    public final static String IOT_REPORT_TOPIC_V1 = "sys/msg/v1";

    /**
     * Redis存IOT采集数据的前缀
     */
    public final static String CACHE_PREFIX = "iot_report_";
    /**
     * 下划线
     */
    public final static String CACHE_UNDERLINE = "_";

    @Autowired
    private MqttHelper mqttHelper;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private ParameterService parameterService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private InfluxHelper influxHelper;


    /**
     * 开始监听MQTT
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
                        ParameterEntity parameterEntity = parameterService.getByCode(payload.getCode());
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
                                lastDataInCache = (String) redisTemplate.opsForValue().get(CACHE_PREFIX + REPORT_KEY_OF_STATUS + CACHE_UNDERLINE + reportData.getDeviceId());
                                if (Objects.nonNull(lastDataInCache) && lastDataInCache.equals(payload.getValue())) {
                                    // 查到了数据 没过期 跳过
                                    continue;
                                }
                                influxHelper.save(payload.getCode(), Integer.parseInt(payload.getValue()), reportData.getDeviceId());
                                redisTemplate.opsForValue().set(CACHE_PREFIX + REPORT_KEY_OF_STATUS + CACHE_UNDERLINE + reportData.getDeviceId(), payload
                                        .getValue());
                                if (Objects.isNull(device)) {
                                    device = deviceService.getByUuid(reportData.getDeviceId());
                                    if (Objects.nonNull(device) && device.getIsReporting()) {
                                        device.setStatus(Integer.parseInt(payload.getValue()));
                                        deviceService.update(device);
                                    }
                                }
                                break;
                            case REPORT_KEY_OF_ALARM:
                                lastDataInCache = (String) redisTemplate.opsForValue().get(CACHE_PREFIX + REPORT_KEY_OF_ALARM + CACHE_UNDERLINE + reportData.getDeviceId());
                                if (Objects.nonNull(lastDataInCache) && lastDataInCache.equals(payload.getValue())) {
                                    // 查到了数据 没过期 跳过
                                    continue;
                                }
                                influxHelper.save(payload.getCode(), Integer.parseInt(payload.getValue()), reportData.getDeviceId());
                                redisTemplate.opsForValue().set(CACHE_PREFIX + REPORT_KEY_OF_ALARM + CACHE_UNDERLINE + reportData.getDeviceId(), payload
                                        .getValue());
                                if (Objects.isNull(device)) {
                                    device = deviceService.getByUuid(reportData.getDeviceId());
                                    if (Objects.nonNull(device) && device.getIsReporting()) {
                                        device.setAlarm(Integer.parseInt(payload.getValue()));
                                        deviceService.update(device);
                                    }
                                }
                                break;
                            case REPORT_KEY_OF_PART_COUNT:
                                lastDataInCache = (String) redisTemplate.opsForValue().get(CACHE_PREFIX + REPORT_KEY_OF_PART_COUNT + CACHE_UNDERLINE + reportData.getDeviceId());
                                if (Objects.nonNull(lastDataInCache) && lastDataInCache.equals(payload.getValue())) {
                                    // 查到了数据 没过期 跳过
                                    continue;
                                }
                                influxHelper.save(payload.getCode(), Double.parseDouble(payload.getValue()), reportData.getDeviceId());
                                redisTemplate.opsForValue().set(CACHE_PREFIX + REPORT_KEY_OF_PART_COUNT + CACHE_UNDERLINE + reportData.getDeviceId(), payload
                                        .getValue());
                                if (Objects.isNull(device)) {
                                    device = deviceService.getByUuid(reportData.getDeviceId());
                                    if (Objects.nonNull(device) && device.getIsReporting()) {
                                        device.setPartCount(Long.parseLong(payload.getValue()));
                                        deviceService.update(device);
                                    }
                                }
                                break;
                            default:
                                influxHelper.save(payload.getCode(), payload.getValue(), reportData.getDeviceId());
                                lastDataInCache = (String) redisTemplate.opsForValue().get(CACHE_PREFIX + payload.getCode() + CACHE_UNDERLINE + reportData.getDeviceId());
                                if (Objects.nonNull(lastDataInCache) && lastDataInCache.equals(payload.getValue())) {
                                    // 查到了数据 没过期 跳过
                                    continue;
                                }
                                redisTemplate.opsForValue().set(CACHE_PREFIX + payload.getCode() + CACHE_UNDERLINE + reportData.getDeviceId(), payload
                                        .getValue());
                        }
                    }
                    reportData.setPayloads(payloadList);
                    redisTemplate.opsForValue().set(CACHE_PREFIX + reportData.getDeviceId(), JSON.toJSONString(reportData));
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }
}
