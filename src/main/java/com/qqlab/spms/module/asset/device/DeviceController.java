package com.qqlab.spms.module.asset.device;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.result.json.Json;
import cn.hamm.airpower.root.RootEntity;
import cn.hamm.airpower.security.Permission;
import com.qqlab.spms.base.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zfy
 */
@RestController
@RequestMapping("device")
@Description("设备")
@Permission(login = false)
public class DeviceController extends BaseController<DeviceEntity, DeviceService, DeviceRepository> {

    @Description("获取实时采集数据")
    @PostMapping("getCurrentCollectionList")
    public Json getCurrentCollectionList(@RequestBody @Validated({RootEntity.WhenIdRequired.class}) DeviceEntity device) {
        return jsonData(service.getCurrentCollectionList(device));
    }
}
