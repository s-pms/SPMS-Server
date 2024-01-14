package com.qqlab.spms.module.asset.device;

import cn.hamm.airpower.result.Result;
import cn.hamm.airpower.util.EnumUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.qqlab.spms.base.BaseService;
import com.qqlab.spms.helper.influxdb.InfluxHelper;
import com.qqlab.spms.module.iot.parameter.ParameterEntity;
import com.qqlab.spms.module.iot.parameter.ParameterService;
import com.qqlab.spms.module.iot.report.*;
import com.qqlab.spms.module.system.coderule.CodeRuleField;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
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
     * 查询指定设备uuid的当前报告
     *
     * @param device 设备
     * @return 报告列表
     */
    public List<ReportPayload> getCurrentReport(DeviceEntity device) {
        device = getById(device.getId());
        Object data = redisTemplate.opsForValue().get(ReportEvent.CACHE_PREFIX + device.getUuid());
        if (Objects.isNull(data)) {
            return new ArrayList<>();
        }
        return JSON.parseObject(data.toString(), ReportData.class).getPayloads();
    }

    /**
     * 通过UUID查询设备
     *
     * @param uuid UUID
     * @return 设备
     */
    public DeviceEntity getByUuid(String uuid) {
        return repository.getByUuid(uuid);
    }

    /**
     * 查询指定设备指定参数的历史记录
     *
     * @param reportPayload 传入参数
     * @return 查询历史
     */
    public List<ReportInfluxPayload> getDevicePayloadHistory(ReportPayload reportPayload) {
        ParameterEntity parameter = parameterService.getByCode(reportPayload.getCode());
        Result.PARAM_INVALID.whenNull(parameter, "不支持的参数");
        ReportGranularity reportGranularity = EnumUtil.getEnumByValue(ReportGranularity.class, reportPayload.getReportGranularity());
        ReportDataType reportDataType = EnumUtil.getEnumByValue(ReportDataType.class, parameter.getDataType());
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
     * 获取设备的参数列表
     *
     * @param device 设备
     * @return 设备
     */
    public DeviceEntity getDeviceParameters(DeviceEntity device) {
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
    protected DeviceEntity beforeSaveToDatabase(DeviceEntity device) {
        if (StrUtil.isBlank(device.getCode())) {
            device.setCode(createCode(CodeRuleField.DeviceCode));
        }
        if (StrUtil.isBlank(device.getUuid())) {
            device.setUuid(device.getCode());
        }
        return super.beforeSaveToDatabase(device);
    }
}
