package cn.hamm.spms.module.system;

import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.web.api.Api;
import cn.hamm.airpower.web.api.ApiController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Slf4j
@Api("wecom")
@Description("企业微信")
public class WecomController extends ApiController {
    @GetMapping("callback")
    public String callback() {
        return "<h1>Server running! </h1>";
    }
}
