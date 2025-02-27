package cn.hamm.spms.module.open.notify;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Extends;
import cn.hamm.airpower.annotation.Permission;
import cn.hamm.airpower.model.Json;
import cn.hamm.airpower.util.DictionaryUtil;
import cn.hamm.spms.base.BaseController;
import cn.hamm.spms.module.open.notify.enums.NotifyScene;
import org.springframework.web.bind.annotation.PostMapping;

import static cn.hamm.airpower.enums.Api.Export;
import static cn.hamm.airpower.enums.Api.QueryExport;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("notify")
@Description("通知钩子")
@Extends(exclude = {Export, QueryExport})
public class NotifyController extends BaseController<NotifyEntity, NotifyService, NotifyRepository> {
    @Description("获取支持通知的场景列表")
    @PostMapping("getSceneList")
    @Permission(authorize = false)
    public Json getSceneList() {
        return Json.data(DictionaryUtil.getDictionaryList(NotifyScene.class));
    }
}
