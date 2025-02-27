package cn.hamm.spms.module.iot.report;

import cn.hamm.airpower.config.Constant;
import cn.hamm.airpower.model.Json;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.common.helper.InfluxHelper;
import cn.hamm.spms.module.asset.device.DeviceEntity;
import cn.hamm.spms.module.asset.device.DeviceService;
import cn.hamm.spms.module.iot.parameter.ParameterEntity;
import cn.hamm.spms.module.iot.parameter.ParameterService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static cn.hamm.spms.module.iot.report.ReportConstant.*;

/**
 * <h1>数据上报的MQ回调</h1>
 *
 * @author Hamm.cn
 */
@Component
@Slf4j
public class ReportMqCallback implements MqttCallback {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private InfluxHelper influxHelper;

    @Override
    public void connectionLost(Throwable throwable) {
    }

    @SuppressWarnings("AlibabaMethodTooLong")
    @Override
    public void messageArrived(String topic, @NotNull MqttMessage mqttMessage) {
        String reportString = new String(mqttMessage.getPayload());
        try {
            ReportData reportData = Json.parse(reportString, ReportData.class);
            DeviceEntity device = null;
            String lastDataInCache;
            List<ReportPayload> payloadList = new ArrayList<>();
            if (Objects.isNull(reportData.getPayloads())) {
                return;
            }
            DeviceService deviceService = Services.getDeviceService();
            ParameterService parameterService = Services.getParameterService();
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
                        lastDataInCache = (String) redisTemplate.opsForValue().get(CACHE_PREFIX + REPORT_KEY_OF_STATUS + Constant.UNDERLINE + reportData.getDeviceId());
                        if (Objects.nonNull(lastDataInCache) && lastDataInCache.equals(payload.getValue())) {
                            // 查到了数据 没过期 跳过
                            continue;
                        }
                        influxHelper.save(payload.getCode(), Integer.parseInt(payload.getValue()), reportData.getDeviceId());
                        redisTemplate.opsForValue().set(CACHE_PREFIX + REPORT_KEY_OF_STATUS + Constant.UNDERLINE + reportData.getDeviceId(), payload
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
                        lastDataInCache = (String) redisTemplate.opsForValue().get(CACHE_PREFIX + REPORT_KEY_OF_ALARM + Constant.UNDERLINE + reportData.getDeviceId());
                        if (Objects.nonNull(lastDataInCache) && lastDataInCache.equals(payload.getValue())) {
                            // 查到了数据 没过期 跳过
                            continue;
                        }
                        influxHelper.save(payload.getCode(), Integer.parseInt(payload.getValue()), reportData.getDeviceId());
                        redisTemplate.opsForValue().set(CACHE_PREFIX + REPORT_KEY_OF_ALARM + Constant.UNDERLINE + reportData.getDeviceId(), payload
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
                        lastDataInCache = (String) redisTemplate.opsForValue().get(CACHE_PREFIX + REPORT_KEY_OF_PART_COUNT + Constant.UNDERLINE + reportData.getDeviceId());
                        if (Objects.nonNull(lastDataInCache) && lastDataInCache.equals(payload.getValue())) {
                            // 查到了数据 没过期 跳过
                            continue;
                        }
                        influxHelper.save(payload.getCode(), Double.parseDouble(payload.getValue()), reportData.getDeviceId());
                        redisTemplate.opsForValue().set(CACHE_PREFIX + REPORT_KEY_OF_PART_COUNT + Constant.UNDERLINE + reportData.getDeviceId(), payload
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
}
