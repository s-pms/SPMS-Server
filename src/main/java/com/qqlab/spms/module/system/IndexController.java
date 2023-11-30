package com.qqlab.spms.module.system;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.root.RootController;
import cn.hamm.airpower.websocket.WebsocketUtil;
import cn.hutool.core.util.RandomUtil;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm
 */
@RestController
@RequestMapping("/")
@Description("首页")
public class IndexController extends RootController {
    @Autowired
    private WebsocketUtil websocketUtil;

    @GetMapping("")
    public String index() throws MqttException {
        websocketUtil.sendToAll("Hi guys helloooooo " + RandomUtil.randomStringUpper(32));
        websocketUtil.sendToUser(1L, "hello 1 " + RandomUtil.randomStringUpper(32));
        websocketUtil.sendToUser(2L, "hello 2 " + RandomUtil.randomStringUpper(32));
        return "<h1>Hello World!</h1>";
    }
}
