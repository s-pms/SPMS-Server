package com.qqlab.spms.module.asset.device;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.security.Permission;
import com.qqlab.spms.base.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zfy
 * @date 2023/11/28
 */
@RestController
@RequestMapping("device")
@Description("设备")
@Permission(login = false)
public class DeviceController extends BaseController<DeviceEntity, DeviceService, DeviceRepository> {

}
