package cn.hamm.spms.module.system;

import cn.hamm.airpower.ai.Ai;
import cn.hamm.airpower.ai.AiRequest;
import cn.hamm.airpower.ai.AiResponse;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.api.Api;
import cn.hamm.airpower.api.ApiController;
import cn.hamm.airpower.api.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

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


    @GetMapping(value = "ai/async", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public ResponseEntity<StreamingResponseBody> aiAsync() {
        StreamingResponseBody responseBody = outputStream -> ai.requestAsync(
                new AiRequest()
                        .addSystemMessage("你的 SPMS 系统的管理员，擅长解决生产各类问题。")
                        .addUserMessage("你是谁？"), stream -> {
                    try {
                        if (!stream.getIsDone()) {
                            String streamMessage = stream.getStreamMessage();
                            System.out.print(streamMessage);
                            outputStream.write(streamMessage.getBytes(StandardCharsets.UTF_8));
                            outputStream.flush();
                        } else {
                            outputStream.close();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8"))
                .body(responseBody);
    }

    @GetMapping(value = "ai/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public ResponseEntity<StreamingResponseBody> aiStream() {
        return ai.requestStream(
                new AiRequest()
                        .addSystemMessage("你的 SPMS 系统的管理员，擅长解决生产各类问题。")
                        .addUserMessage("你是谁？"), stream -> {
                    System.out.println(stream.getIsDone());
                    return Json.toString(Map.of(
                            "done", stream.getIsDone(),
                            "message", stream.getIsDone() ? "" : stream.getStreamMessage()
                    ));
                });
    }
}
