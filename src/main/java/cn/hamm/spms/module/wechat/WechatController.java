package cn.hamm.spms.module.wechat;

import cn.hamm.airpower.annotation.Permission;
import cn.hamm.airpower.root.RootController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <h1>Wechat</h1>
 *
 * @author Hamm.cn
 */
@Permission(login = false)
@Controller
@RequestMapping("wechat")
public class WechatController extends RootController {
    /**
     * {@code Success}
     */
    public static final String STRING_SUCCESS = "success";

    @RequestMapping(value = "init", produces = "text/plain")
    @ResponseBody
    public String init() {
        return STRING_SUCCESS;
    }
}
