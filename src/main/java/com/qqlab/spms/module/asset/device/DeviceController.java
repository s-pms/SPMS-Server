package com.qqlab.spms.module.asset.device;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.mqtt.MqttHelper;
import cn.hamm.airpower.result.json.Json;
import cn.hamm.airpower.root.RootEntity;
import cn.hamm.airpower.security.Permission;
import cn.hutool.core.util.RandomUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qqlab.spms.base.BaseController;
import com.qqlab.spms.module.iot.report.ReportData;
import com.qqlab.spms.module.iot.report.ReportType;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    @Autowired
    private MqttHelper mqttHelper;

    @Description("获取实时采集数据")
    @PostMapping("getCurrentCollectionList")
    public Json getCurrentCollectionList(@RequestBody @Validated({RootEntity.WhenIdRequired.class}) DeviceEntity device) throws MqttException, JsonProcessingException {

        ReportData reportData = new ReportData()
                .setUuid("DE202312040001").setType(ReportType.STATUS).setValue(String.valueOf(RandomUtil.randomInt(1, 5)));
        String msg = new ObjectMapper().writeValueAsString(reportData);
        mqttHelper.publish("IOT", msg);
        return jsonData(service.getCurrentCollectionList(device));
    }
}
