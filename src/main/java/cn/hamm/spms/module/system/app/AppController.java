package cn.hamm.spms.module.system.app;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.response.Filter;
import cn.hamm.airpower.result.json.JsonData;
import cn.hamm.airpower.root.RootEntity;
import cn.hamm.airpower.security.Permission;
import cn.hamm.spms.base.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm
 */
@RestController
@RequestMapping("app")
@Description("应用")
public class AppController extends BaseController<AppEntity, AppService, AppRepository> {
    @Description("通过AppKey获取应用信息")
    @RequestMapping("getByAppKey")
    @Permission(login = false)
    @Filter(RootEntity.WhenGetDetail.class)
    public JsonData getByAppKey(@RequestBody @Validated({AppEntity.WhenGetByAppKey.class}) AppEntity entity) {
        return jsonData(service.getByAppKey(entity.getAppKey()));
    }

    @Description("重置指定应用的秘钥")
    @RequestMapping("resetSecret")
    public JsonData resetSecret(@RequestBody @Validated({AppEntity.WhenResetSecret.class}) AppEntity entity) {
        return jsonData(service.resetSecretById(entity.getId()), "重置应用秘钥成功");
    }
}
