package cn.hamm.spms.module.iot.report;

import cn.hamm.airpower.core.Json;
import cn.hamm.airpower.redis.RedisHelper;
import cn.hamm.spms.common.influx.InfluxHelper;
import cn.hamm.spms.module.asset.AssetServices;
import cn.hamm.spms.module.asset.device.DeviceEntity;
import cn.hamm.spms.module.asset.device.DeviceService;
import cn.hamm.spms.module.iot.IotServices;
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
 * <h1>设备数据上报回调</h1>
 *
 * @author Hamm.cn
 * @apiNote 单条上报里的每个参数各自 try-catch，某个参数解析失败不影响同批其他参数；
 * 三个系统参数（{@code Status} / {@code Alarm} / {@code PartCnt}）除了写 InfluxDB
 * 还要回写 {@code DeviceEntity} 对应字段，其余参数只入 InfluxDB
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

    /**
     * 处理一条设备上报报文
     *
     * @param topic       上报 Topic
     * @param mqttMessage 报文
     * @apiNote 会跳过值为空、未登记为采集参数、以及 5 秒内值未变化的项
     */
    @Override
    public void messageArrived(String topic, @NotNull MqttMessage mqttMessage) {
        String reportString = new String(mqttMessage.getPayload());
        try {
            ReportData reportData = Json.parse(reportString, ReportData.class);
            log.info("数据上报: {}", Json.toString(reportData));
            if (Objects.isNull(reportData.getPayloads())) {
                return;
            }
            ParameterService parameterService = IotServices.getParameterService();
            String uuid = reportData.getDeviceId();
            DeviceService deviceService = AssetServices.getDeviceService();
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
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 设备非空时执行赋值
     *
     * @param device   设备
     * @param function 赋值动作
     * @param value    赋的值
     * @param <T>      值的类型
     */
    private <T> void saveIfNotNull(DeviceEntity device, BiFunction<DeviceEntity, T, DeviceEntity> function, T value) {
        Optional.ofNullable(device).ifPresent(d -> function.apply(d, value));
    }

    /**
     * 缓存设备指定参数本次上报的值
     *
     * @param code        参数编码
     * @param uuid        设备的 UUID
     * @param reportValue 上报的数据
     * @apiNote TTL 5 秒，仅用于相邻报文去重，不代表设备当前真实状态
     */
    private void saveLastReportParameterValue(String code, String uuid, String reportValue) {
        redisHelper.set(getDeviceReportParamCacheKey(code, uuid), reportValue, 5);
    }

    /**
     * 获取设备指定参数上一次上报的值
     *
     * @param code 参数编码
     * @param uuid 设备的 UUID
     * @return 上报的值，缓存已过期返回 {@code null}
     */
    private @Nullable String getLastDataInCache(String code, String uuid) {
        Object object = redisHelper.get(getDeviceReportParamCacheKey(code, uuid));
        return object == null ? null : object.toString();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
