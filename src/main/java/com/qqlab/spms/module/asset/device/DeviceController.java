package com.qqlab.spms.module.asset.device;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.result.Result;
import cn.hamm.airpower.result.json.Json;
import cn.hamm.airpower.root.RootEntity;
import cn.hamm.airpower.security.Permission;
import com.qqlab.spms.base.BaseController;
import com.qqlab.spms.module.iot.parameter.ParameterEntity;
import com.qqlab.spms.module.iot.parameter.ParameterService;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private ParameterService parameterService;

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
        Result.NOT_FOUND.whenNull(device);
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

    @Override
    protected DeviceEntity beforeUpdate(DeviceEntity device) {
        return service.getDeviceParameters(device);
    }

    @Override
    protected DeviceEntity beforeAdd(DeviceEntity device) {
        return service.getDeviceParameters(device);
    }

}
