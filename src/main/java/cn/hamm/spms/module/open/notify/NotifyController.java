package cn.hamm.spms.module.open.notify;

import cn.hamm.airpower.core.DictionaryUtil;
import cn.hamm.airpower.core.Json;
import cn.hamm.airpower.core.annotation.Description;
import cn.hamm.airpower.web.access.Permission;
import cn.hamm.airpower.web.api.Api;
import cn.hamm.airpower.web.api.Extends;
import cn.hamm.airpower.web.curd.Curd;
import cn.hamm.spms.base.BaseController;
import cn.hamm.spms.module.open.notify.enums.NotifyScene;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("notify")
@Description("通知钩子")
@Extends({Curd.Disable, Curd.Enable})
public class NotifyController extends BaseController<NotifyEntity, NotifyService, NotifyRepository> {
    @Description("获取支持通知的场景列表")
    @PostMapping("getSceneList")
    @Permission(authorize = false)
    public Json getSceneList() {
        return Json.data(DictionaryUtil.getDictionaryList(NotifyScene.class));
    }
}
