package com.qqlab.spms.module.wechat;

import cn.hamm.airpower.root.RootController;
import cn.hamm.airpower.security.Permission;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <h1>Wechat</h1>
 *
 * @author Hamm
 */
@Permission(login = false)
@Controller
@RequestMapping("wechat")
public class WechatController extends RootController {
    @RequestMapping(value = "init", produces = "text/plain")
    @ResponseBody
    public String init() {
        return "success";
    }
}
