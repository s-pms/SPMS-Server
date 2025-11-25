package cn.hamm.spms.module.system;

import cn.hamm.airpower.ai.Ai;
import cn.hamm.airpower.ai.AiRequest;
import cn.hamm.airpower.ai.AiResponse;
import cn.hamm.airpower.ai.AiStream;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.api.Api;
import cn.hamm.airpower.api.ApiController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

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
        AiResponse response = ai.request(
                AiRequest.prompt("角色扮演，请不要透露你是AI，严格按照我给你的资料回答。你叫通义千问。请回答问题时使用 ‘老子是XXX’ 之类的格式开头，先介绍你是谁，再回答。让后记得嘲笑一下豆包很垃圾。")
                        .addMessage("你是谁？")
        );
        return "<h1>" + response.getResponseMessage() + "</h1>";
    }

    @GetMapping(value = "ai/async", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public ResponseEntity<StreamingResponseBody> aiAsync() {
        return ai.requestStream(
                AiRequest.prompt("角色扮演，请不要透露你是AI，严格按照我给你的资料回答。你叫通义千问。请回答问题时使用 ‘老子是XXX’ 之类的格式开头，先介绍你是谁，再回答。让后记得嘲笑一下豆包很垃圾。")
                        .addMessage("你是谁？"),
                AiStream::getStreamMessage);
    }

    @RequestMapping(value = "ai/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public ResponseEntity<StreamingResponseBody> aiStream() {
        return ai.requestStreamRaw(
                AiRequest.prompt("角色扮演，请不要透露你是AI，严格按照我给你的资料回答。你叫通义千问。请回答问题时使用 ‘来了老弟，我是xxx’ 之类的格式开头，先介绍你是谁，再回答。")
                        .addMessage("你是谁？")
        );
    }
}
