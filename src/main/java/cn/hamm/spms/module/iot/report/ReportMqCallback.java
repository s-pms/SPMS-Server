package cn.hamm.spms.module.iot.report;

import cn.hamm.airpower.core.Json;
import cn.hamm.airpower.web.redis.RedisHelper;
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
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

import static cn.hamm.spms.module.iot.report.ReportConstant.*;

/**
 * <h1>数据上报的 MQ 回调</h1>
 *
 * @author Hamm.cn
 */
@Component
@Slf4j
public class ReportMqCallback implements MqttCallback {
    @Resource
    private RedisHelper redisHelper;

    @Autowired
    private InfluxHelper influxHelper;

    @Override
    public void connectionLost(Throwable throwable) {
    }

    @Override
    public void messageArrived(String topic, @NotNull MqttMessage mqttMessage) {
        String reportString = new String(mqttMessage.getPayload());
        try {
            ReportData reportData = Json.parse(reportString, ReportData.class);
            log.info("数据上报: {}", Json.toString(reportData));
            if (Objects.isNull(reportData.getPayloads())) {
                return;
            }
            ParameterService parameterService = Services.getParameterService();
            String uuid = reportData.getDeviceId();
            DeviceService deviceService = Services.getDeviceService();
            DeviceEntity device = deviceService.getByUuid(uuid);
            if (Objects.isNull(device)) {
                log.info("设备不存在: {}", uuid);
                return;
            }
            List<ReportPayload> payloadList = new ArrayList<>();
            for (ReportPayload payload : reportData.getPayloads()) {
                String reportValue = payload.getValue();
                if (Objects.isNull(reportValue)) {
                    continue;
                }
                String parameterCode = payload.getCode();
                String lastDataInCache = getLastDataInCache(parameterCode, uuid);
                if (Objects.nonNull(lastDataInCache) && lastDataInCache.equals(reportValue)) {
                    // 查到了数据 没过期 跳过
                    continue;
                }
                ParameterEntity parameter = parameterService.getByCode(parameterCode);
                if (Objects.isNull(parameter)) {
                    continue;
                }
                payloadList.add(new ReportPayload()
                        .setCode(parameterCode)
                        .setValue(reportValue)
                        .setLabel(parameter.getLabel())
                        .setDataType(parameter.getDataType()));
                saveLastReportParameterValue(parameterCode, uuid, reportValue);
                try {
                    int intValue;
                    switch (parameterCode) {
                        case REPORT_KEY_OF_STATUS:
                            intValue = Integer.parseInt(reportValue);
                            saveIfNotNull(device, DeviceEntity::setStatus, intValue);
                            influxHelper.save(parameterCode, uuid, intValue);
                            break;
                        case REPORT_KEY_OF_ALARM:
                            intValue = Integer.parseInt(reportValue);
                            saveIfNotNull(device, DeviceEntity::setAlarm, intValue);
                            influxHelper.save(parameterCode, uuid, intValue);
                            break;
                        case REPORT_KEY_OF_PART_COUNT:
                            long longValue = Long.parseLong(reportValue);
                            saveIfNotNull(device, DeviceEntity::setPartCount, longValue);
                            influxHelper.save(parameterCode, uuid, longValue);
                            break;
                        default:
                            influxHelper.save(parameterCode, uuid, reportValue);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            deviceService.update(device);
            reportData.setPayloads(payloadList);
            redisHelper.set(getDeviceReportCacheKey(uuid), Json.toString(reportData));
        } catch (java.lang.Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 保存数据
     *
     * @param device   设备
     * @param function 设置的函数
     * @param value    保存的值
     * @param <T>      保存值的类型
     */
    private <T> void saveIfNotNull(DeviceEntity device, BiFunction<DeviceEntity, T, DeviceEntity> function, T value) {
        Optional.ofNullable(device).ifPresent(d -> function.apply(d, value));
    }

    /**
     * 缓存设备指定参数的数据
     *
     * @param code        参数编码
     * @param uuid        设备的 UUID
     * @param reportValue 上报的数据
     */
    private void saveLastReportParameterValue(String code, String uuid, String reportValue) {
        redisHelper.set(getDeviceReportParamCacheKey(code, uuid), reportValue, 5);
    }

    /**
     * 获取设备指定参数的缓存数据
     *
     * @param code 参数 Key
     * @param uuid 设备的 UUID
     * @return 上报的数据
     */
    private @Nullable String getLastDataInCache(String code, String uuid) {
        Object object = redisHelper.get(getDeviceReportParamCacheKey(code, uuid));
        return object == null ? null : object.toString();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
