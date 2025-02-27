package cn.hamm.spms.module.asset.device;

import cn.hamm.airpower.exception.ServiceError;
import cn.hamm.airpower.model.Json;
import cn.hamm.airpower.util.DictionaryUtil;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.common.helper.InfluxHelper;
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
import java.util.stream.Collectors;

import static cn.hamm.spms.module.iot.report.ReportConstant.*;

/**
 * <h1>Service</h1>
 *
 * @author zfy
 */
@Service
public class DeviceService extends BaseService<DeviceEntity, DeviceRepository> {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private InfluxHelper influxHelper;

    /**
     * <h3>查询指定设备uuid的当前报告</h3>
     *
     * @param deviceId 设备ID
     * @return 报告列表
     */
    public List<ReportPayload> getCurrentReport(long deviceId) {
        DeviceEntity device = get(deviceId);
        Object data = redisTemplate.opsForValue().get(ReportConstant.getDeviceReportCacheKey(device.getUuid()));
        if (Objects.isNull(data)) {
            return new ArrayList<>();
        }
        return Objects.requireNonNull(Json.parse(data.toString(), ReportData.class)).getPayloads();
    }

    /**
     * <h3>通过UUID查询设备</h3>
     *
     * @param uuid UUID
     * @return 设备
     */
    public DeviceEntity getByUuid(String uuid) {
        return repository.getByUuid(uuid);
    }

    /**
     * <h3>查询指定设备指定参数的历史记录</h3>
     *
     * @param reportPayload 传入参数
     * @return 查询历史
     */
    public List<ReportInfluxPayload> getDevicePayloadHistory(@NotNull ReportPayload reportPayload) {
        ParameterEntity parameter = Services.getParameterService().getByCode(reportPayload.getCode());
        ServiceError.PARAM_INVALID.whenNull(parameter, "不支持的参数");
        ReportGranularity reportGranularity = DictionaryUtil.getDictionary(ReportGranularity.class, reportPayload.getReportGranularity());
        ReportDataType reportDataType = DictionaryUtil.getDictionary(ReportDataType.class, parameter.getDataType());
        return switch (reportDataType) {
            case QUANTITY -> influxHelper.queryQuantity(reportPayload, reportGranularity);
            case STATUS -> influxHelper.queryStatus(reportPayload, reportGranularity);
            case SWITCH -> influxHelper.querySwitch(reportPayload, reportGranularity);
            default -> influxHelper.queryInformation(reportPayload, reportGranularity);
        };
    }

    /**
     * <h3>获取设备的参数列表</h3>
     *
     * @param device 设备
     * @return 设备
     */
    public DeviceEntity getDeviceParameters(@NotNull DeviceEntity device) {
        Set<ParameterEntity> parameters = new HashSet<>();
        ParameterService parameterService = Services.getParameterService();
        if (Objects.nonNull(device.getParameters())) {
            parameters = device.getParameters().stream()
                    .map(parameter -> parameterService.get(parameter.getId()))
                    .filter(parameter -> !parameter.getIsSystem())
                    .collect(Collectors.toSet());
        }
        parameters.add(parameterService.getByCode(REPORT_KEY_OF_STATUS));
        parameters.add(parameterService.getByCode(REPORT_KEY_OF_ALARM));
        parameters.add(parameterService.getByCode(REPORT_KEY_OF_PART_COUNT));
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
