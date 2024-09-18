package cn.hamm.spms.module.webhook;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Permission;
import cn.hamm.airpower.interfaces.IEntityAction;
import cn.hamm.airpower.model.Json;
import cn.hamm.spms.base.BaseController;
import cn.hamm.spms.module.webhook.enums.WebHookScene;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Arrays;
import java.util.Map;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("webhook")
@Description("通知钩子")
public class WebHookController extends BaseController<WebHookEntity, WebHookService, WebHookRepository> implements IEntityAction {
    @Description("获取支持通知的场景列表")
    @PostMapping("getSceneList")
    @Permission(authorize = false)
    public Json getSceneList() {
        return Json.data(Arrays.stream(WebHookScene.values())
                .map(item -> Map.of(
                        "key", item.getKey(),
                        "label", item.getLabel()
                ))
                .toList()
        );
    }
}
