package com.qqlab.spms.module.index;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.root.RootController;
import cn.hamm.airpower.websocket.WebsocketUtil;
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
    public String index() {
        websocketUtil.sendToAll("Hi guys helloooooo");
        websocketUtil.sendToUser(1L, "hello 1");
        websocketUtil.sendToUser(2L, "hello 2");
        return "<h1>Hello World!</h1>";
    }
}
