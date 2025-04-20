package cn.hamm.spms.module.system;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.api.Api;
import cn.hamm.airpower.api.ApiController;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("/")
@Description("首页")
public class IndexController extends ApiController {
    @GetMapping("")
    public String index() {
        return "<h1>Server running!</h1>";
    }
}
