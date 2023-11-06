package cn.hamm.starter.module.wechat;

import cn.hamm.airpower.root.RootController;
import cn.hamm.airpower.security.Permission;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

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
    public String init() {
        return "success";
    }
}
