package cn.hamm.spms.module.asset.device;


import cn.hamm.airpower.enums.Result;
import cn.hamm.airpower.model.json.Json;
import cn.hamm.airpower.util.AirUtil;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.common.helper.influxdb.InfluxHelper;
import cn.hamm.spms.module.iot.parameter.ParameterEntity;
import cn.hamm.spms.module.iot.parameter.ParameterService;
import cn.hamm.spms.module.iot.report.*;
import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * <h1>Service</h1>
 *
 * @author zfy
 */
@Service
public class DeviceService extends BaseService<DeviceEntity, DeviceRepository> {
    @Autowired
    private ParameterService parameterService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private InfluxHelper influxHelper;

    /**
     * <h2>查询指定设备uuid的当前报告</h2>
     *
     * @param deviceId 设备ID
     * @return 报告列表
     */
    public List<ReportPayload> getCurrentReport(long deviceId) {
        DeviceEntity device = get(deviceId);
        Object data = redisTemplate.opsForValue().get(ReportEvent.CACHE_PREFIX + device.getUuid());
        if (Objects.isNull(data)) {
            return new ArrayList<>();
        }
        return Objects.requireNonNull(Json.parse(data.toString(), ReportData.class)).getPayloads();
    }

    /**
     * <h2>通过UUID查询设备</h2>
     *
     * @param uuid UUID
     * @return 设备
     */
    public DeviceEntity getByUuid(String uuid) {
        return repository.getByUuid(uuid);
    }

    /**
     * <h2>查询指定设备指定参数的历史记录</h2>
     *
     * @param reportPayload 传入参数
     * @return 查询历史
     */
    public List<ReportInfluxPayload> getDevicePayloadHistory(@NotNull ReportPayload reportPayload) {
        ParameterEntity parameter = parameterService.getByCode(reportPayload.getCode());
        Result.PARAM_INVALID.whenNull(parameter, "不支持的参数");
        ReportGranularity reportGranularity = AirUtil.getDictionaryUtil().getDictionaryByKey(ReportGranularity.class, reportPayload.getReportGranularity());
        ReportDataType reportDataType = AirUtil.getDictionaryUtil().getDictionaryByKey(ReportDataType.class, parameter.getDataType());
        if (reportDataType != null) {
            switch (reportDataType) {
                case QUANTITY:
                    return influxHelper.queryQuantity(reportPayload, reportGranularity);
                case STATUS:
                    return influxHelper.queryStatus(reportPayload, reportGranularity);
                case SWITCH:
                    return influxHelper.querySwitch(reportPayload, reportGranularity);
                default:
            }
        }
        return influxHelper.queryInformation(reportPayload, reportGranularity);
    }

    /**
     * <h2>获取设备的参数列表</h2>
     *
     * @param device 设备
     * @return 设备
     */
    public DeviceEntity getDeviceParameters(@NotNull DeviceEntity device) {
        Set<ParameterEntity> parameters = new HashSet<>();
        if (Objects.nonNull(device.getParameters())) {
            for (ParameterEntity parameter : device.getParameters()) {
                parameter = parameterService.get(parameter.getId());
                if (!parameter.getIsSystem()) {
                    parameters.add(parameter);
                }
            }
        }
        parameters.add(parameterService.getByCode(ReportEvent.REPORT_KEY_OF_STATUS));
        parameters.add(parameterService.getByCode(ReportEvent.REPORT_KEY_OF_ALARM));
        parameters.add(parameterService.getByCode(ReportEvent.REPORT_KEY_OF_PART_COUNT));
        device.setParameters(parameters);
        return device;
    }

    @Override
    protected DeviceEntity beforeAppSaveToDatabase(@NotNull DeviceEntity device) {
        if (!StringUtils.hasText(device.getUuid())) {
            device.setUuid(device.getCode());
        }
        return device;
    }
}
