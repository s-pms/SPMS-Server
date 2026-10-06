package cn.hamm.spms.module.asset.device;

import cn.hamm.airpower.api.annotation.Api;
import cn.hamm.airpower.core.Json;
import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.core.annotation.ExposeAll;
import cn.hamm.airpower.curd.permission.Permission;
import cn.hamm.airpower.redis.RedisHelper;
import cn.hamm.spms.base.BaseController;
import cn.hamm.spms.module.iot.parameter.ParameterEntity;
import cn.hamm.spms.module.iot.report.IReportPayloadAction;
import cn.hamm.spms.module.iot.report.ReportPayload;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static cn.hamm.airpower.exception.Errors.DATA_NOT_FOUND;
import static cn.hamm.spms.module.iot.report.ReportConstant.getDeviceReportCacheKey;

/**
 * <h1>设备</h1>
 *
 * @author zfy
 */
@Slf4j
@Api("device")
@Description("设备")
public class DeviceController extends BaseController<
        DeviceEntity, DeviceService, DeviceRepository
        > implements IDeviceAction, IReportPayloadAction {
    @Resource
    private RedisHelper redisHelper;

    @Override
    protected DeviceEntity afterGetDetail(@NotNull DeviceEntity device) {
        return service.getDeviceParameters(device);
    }

    @Description("获取实时采集数据")
    @PostMapping("getCurrentReport")
    @Permission(authorize = false)
    @ExposeAll({ReportPayload.class})
    public Json getCurrentReport(@RequestBody @Validated(WhenIdRequired.class) DeviceEntity device) {
        return Json.data(service.getCurrentReport(device.getId()));
    }

    @Description("获取采集配置")
    @PostMapping("getDeviceConfig")
    @Permission(login = false)
    public Json getDeviceConfig(@RequestBody @Validated(WhenGetDeviceConfig.class) DeviceEntity device) {
        device = service.getByUuid(device.getUuid());
        DATA_NOT_FOUND.whenNull(device);
        device.excludeNotMeta();
        // 参数只下发编码与标题：ID、类型、内置标志都属于服务端内部信息，
        // 对外暴露会让客户端知道可以传哪些 code 去查时序库
        Set<ParameterEntity> parameters = new HashSet<>();
        device = service.getDeviceParameters(device);
        device.getParameters().forEach(p -> {
            if (Objects.isNull(p)) {
                return;
            }
            parameters.add(new ParameterEntity()
                    .setCode(p.getCode())
                    .setLabel(p.getLabel()));
        });
        device.setParameters(parameters);
        return Json.data(device);
    }

    @Description("获取指定设备某个参数的历史")
    @PostMapping("getDevicePayloadHistory")
    @Permission(authorize = false)
    public Json getDevicePayloadHistory(@RequestBody @Validated(WhenGetDevicePayloadHistory.class) ReportPayload payload) {
        return Json.data(service.getDevicePayloadHistory(payload));
    }

    @Override
    protected DeviceEntity beforeAppUpdate(@NotNull DeviceEntity device, @NotNull DeviceEntity exist) {
        // 只能用库里的 uuid：前端不回传或传错 uuid 时，真实那条实时数据缓存不会被清掉
        redisHelper.delete(getDeviceReportCacheKey(exist.getUuid()));
        // 设备换了 uuid 时旧 uuid 的缓存也要清，否则会一直命中脏数据
        if (Objects.nonNull(exist.getUuid()) && !Objects.equals(exist.getUuid(), device.getUuid())) {
            redisHelper.delete(getDeviceReportCacheKey(exist.getUuid()));
            log.info("设备 {} 的 uuid 从 {} 变为 {}，已清理旧 uuid 的实时数据缓存",
                    exist.getId(), exist.getUuid(), device.getUuid());
        }
        return service.getDeviceParameters(device);
    }

    @Override
    protected DeviceEntity beforeAppAdd(@NotNull DeviceEntity device) {
        return service.getDeviceParameters(device);
    }
}
