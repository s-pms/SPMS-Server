package cn.hamm.spms.module.wechat;

import cn.hamm.airpower.access.Permission;
import cn.hamm.airpower.api.ApiController;
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
public class WechatController extends ApiController {
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
