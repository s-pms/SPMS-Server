package cn.hamm.spms.module.wechat;

import cn.hamm.airpower.web.annotation.Permission;
import cn.hamm.airpower.web.model.RootController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static cn.hamm.airpower.core.constant.Constant.STRING_SUCCESS;

/**
 * <h1>Wechat</h1>
 *
 * @author Hamm.cn
 */
@Permission(login = false)
@Controller
@RequestMapping("wechat")
public class WechatController extends RootController {
    @RequestMapping(value = "init", produces = "text/plain")
    @ResponseBody
    public String init() {
        return STRING_SUCCESS;
    }
}
