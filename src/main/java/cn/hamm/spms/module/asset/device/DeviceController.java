package cn.hamm.spms.module.asset.device;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Permission;
import cn.hamm.airpower.enums.ServiceError;
import cn.hamm.airpower.model.Json;
import cn.hamm.spms.base.BaseController;
import cn.hamm.spms.module.iot.parameter.ParameterEntity;
import cn.hamm.spms.module.iot.report.IReportPayloadAction;
import cn.hamm.spms.module.iot.report.ReportEvent;
import cn.hamm.spms.module.iot.report.ReportPayload;
import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.HashSet;
import java.util.Set;

/**
 * <h1>Controller</h1>
 *
 * @author zfy
 */
@ApiController("device")
@Description("设备")
public class DeviceController extends BaseController<DeviceEntity, DeviceService, DeviceRepository> implements IDeviceAction, IReportPayloadAction {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    protected DeviceEntity afterGetDetail(DeviceEntity device) {
        return service.getDeviceParameters(device);
    }

    @Description("获取实时采集数据")
    @PostMapping("getCurrentReport")
    @Permission(login = false)
    public Json getCurrentReport(@RequestBody @Validated(WhenIdRequired.class) DeviceEntity device) {
        return Json.data(service.getCurrentReport(device.getId()));
    }

    @Description("获取采集配置")
    @PostMapping("getDeviceConfig")
    @Permission(login = false)
    public Json getDeviceConfig(@RequestBody @Validated(WhenGetDeviceConfig.class) DeviceEntity device) {
        device = service.getByUuid(device.getUuid());
        ServiceError.DATA_NOT_FOUND.whenNull(device);
        device.setPartCount(null)
                .setAlarm(null)
                .setStatus(null)
                .excludeBaseData();
        Set<ParameterEntity> parameters = new HashSet<>();
        device = service.getDeviceParameters(device);
        device.getParameters().forEach(p -> {
            p.setId(null).excludeBaseData();
            parameters.add(p);
        });
        device.setParameters(parameters);
        return Json.data(device);
    }

    @Description("获取指定设备某个参数的历史")
    @PostMapping("getDevicePayloadHistory")
    @Permission(login = false)
    public Json getDevicePayloadHistory(@RequestBody @Validated(WhenGetDevicePayloadHistory.class) ReportPayload payload) {
        return Json.data(service.getDevicePayloadHistory(payload));
    }

    @Override
    protected DeviceEntity beforeUpdate(@NotNull DeviceEntity device) {
        redisTemplate.delete(ReportEvent.CACHE_PREFIX + device.getUuid());
        return service.getDeviceParameters(device);
    }

    @Override
    protected DeviceEntity beforeAdd(DeviceEntity device) {
        return service.getDeviceParameters(device);
    }
}
