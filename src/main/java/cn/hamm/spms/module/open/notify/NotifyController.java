package cn.hamm.spms.module.open.notify;

import cn.hamm.airpower.access.Permission;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.api.Api;
import cn.hamm.airpower.api.Extends;
import cn.hamm.airpower.api.Json;
import cn.hamm.airpower.dictionary.DictionaryUtil;
import cn.hamm.spms.base.BaseController;
import cn.hamm.spms.module.open.notify.enums.NotifyScene;
import org.springframework.web.bind.annotation.PostMapping;

import static cn.hamm.airpower.curd.Curd.Export;
import static cn.hamm.airpower.curd.Curd.QueryExport;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@Api("notify")
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
