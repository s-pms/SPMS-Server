package cn.hamm.spms.module.system;

import cn.hamm.airpower.ai.Ai;
import cn.hamm.airpower.ai.AiRequest;
import cn.hamm.airpower.ai.AiResponse;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.api.Api;
import cn.hamm.airpower.api.ApiController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("/")
@Description("首页")
public class IndexController extends ApiController {
    @Autowired
    private Ai ai;

    @GetMapping("")
    public String index() {
        return "<h1>Server running!</h1>";
    }

    @GetMapping("ai")
    public String ai() {
        AiResponse response = ai.request(new AiRequest()
                .addSystemMessage("你的 SPMS 系统的管理员，擅长解决生产各类问题。")
                .addUserMessage("你是谁？")
        );
        return "<h1>" + response.getResponseMessage() + "</h1>";
    }
}
