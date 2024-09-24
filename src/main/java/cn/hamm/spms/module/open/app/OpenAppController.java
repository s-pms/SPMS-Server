package cn.hamm.spms.module.open.app;

import cn.hamm.airpower.annotation.ApiController;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.annotation.Filter;
import cn.hamm.airpower.annotation.Permission;
import cn.hamm.airpower.model.Json;
import cn.hamm.airpower.root.RootEntity;
import cn.hamm.spms.base.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Base64;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("open/app")
@Description("开放应用")
public class OpenAppController extends BaseController<OpenAppEntity, OpenAppService, OpenAppRepository> implements IOpenAppAction {
    @Description("通过AppKey获取应用信息")
    @PostMapping("getByAppKey")
    @Permission(login = false)
    @Filter(RootEntity.WhenGetDetail.class)
    public Json getByAppKey(@RequestBody @Validated(WhenGetByAppKey.class) OpenAppEntity entity) {
        return Json.data(service.getByAppKey(entity.getAppKey()));
    }

    @Override
    public Json add(@RequestBody @Validated(WhenAdd.class) OpenAppEntity openApp) {
        openApp = service.get(service.add(openApp));
        return Json.data(String.format("应用名称: %s\n\nAppKey:\n%s\n\nAppSecret:\n%s\n\n公钥:\n%s", openApp.getAppName(), openApp.getAppKey(), openApp.getAppSecret(), openApp.getPublicKey()));
    }

    @Override
    protected void afterAdd(long id, OpenAppEntity source) {
        super.afterAdd(id, source);
    }

    @Description("重置密钥")
    @PostMapping("resetSecret")
    public Json resetSecret(@RequestBody @Validated(WhenIdRequired.class) OpenAppEntity openApp) {
        OpenAppEntity exist = service.get(openApp.getId());
        String appSecret = Base64.getEncoder().encodeToString(randomUtil.randomBytes());
        exist.setAppSecret(appSecret);
        service.update(exist);
        return Json.data(appSecret);
    }

    @Description("重置密钥对")
    @PostMapping("resetKeyPair")
    public Json resetKeyPair(@RequestBody @Validated(WhenIdRequired.class) OpenAppEntity openApp) {
        OpenAppEntity exist = service.get(openApp.getId());
        service.resetKeyPare(exist);
        service.update(exist);
        return Json.data(exist.getPublicKey());
    }
}
