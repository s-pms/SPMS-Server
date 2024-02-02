package com.qqlab.spms.module.asset.device;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.result.Result;
import cn.hamm.airpower.result.json.Json;
import cn.hamm.airpower.root.RootEntity;
import cn.hamm.airpower.security.Permission;
import com.qqlab.spms.base.BaseController;
import com.qqlab.spms.module.iot.parameter.ParameterEntity;
import com.qqlab.spms.module.iot.report.ReportEvent;
import com.qqlab.spms.module.iot.report.ReportPayload;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Set;

/**
 * @author zfy
 */
@RestController
@RequestMapping("device")
@Description("设备")
@Permission(login = false)
public class DeviceController extends BaseController<DeviceEntity, DeviceService, DeviceRepository> {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    protected DeviceEntity afterGetDetail(DeviceEntity device) {
        return service.getDeviceParameters(device);
    }

    @Description("获取实时采集数据")
    @PostMapping("getCurrentReport")
    public Json getCurrentReport(@RequestBody @Validated({RootEntity.WhenIdRequired.class}) DeviceEntity device) {
        return jsonData(service.getCurrentReport(device));
    }

    @Description("获取采集配置")
    @PostMapping("getDevice")
    @Permission(login = false)
    public Json getDevice(@RequestBody @Validated({DeviceEntity.WhenGetDevice.class}) DeviceEntity device) {
        device = service.getByUuid(device.getUuid());
        Result.DATA_NOT_FOUND.whenNull(device);
        device = device.setPartCount(null)
                .setAlarm(null)
                .setStatus(null)
                .excludeBaseData();
        Set<ParameterEntity> parameters = new HashSet<>();
        device = service.getDeviceParameters(device);
        for (ParameterEntity p : device.getParameters()) {
            p.setId(null).excludeBaseData();
            parameters.add(p);
        }
        device.setParameters(parameters);
        return jsonData(device);
    }

    @Description("获取指定设备某个参数的历史")
    @PostMapping("getDevicePayloadHistory")
    @Permission(login = false)
    public Json getDevicePayloadHistory(@RequestBody @Validated({ReportPayload.WhenGetDevicePayloadHistory.class}) ReportPayload payload) {
        return jsonData(service.getDevicePayloadHistory(payload));
    }

    @Override
    protected DeviceEntity beforeUpdate(DeviceEntity device) {
        redisTemplate.delete(ReportEvent.CACHE_PREFIX + device.getUuid());
        return service.getDeviceParameters(device);
    }

    @Override
    protected DeviceEntity beforeAdd(DeviceEntity device) {
        return service.getDeviceParameters(device);
    }

}
