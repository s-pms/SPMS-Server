package cn.hamm.spms.module.open.app;

import cn.hamm.airpower.annotation.*;
import cn.hamm.airpower.enums.Api;
import cn.hamm.airpower.exception.ServiceError;
import cn.hamm.airpower.model.Json;
import cn.hamm.airpower.root.RootEntity;
import cn.hamm.airpower.util.RandomUtil;
import cn.hamm.spms.base.BaseController;
import cn.hamm.spms.module.open.notify.NotifyService;
import cn.hamm.spms.module.open.notify.enums.NotifyScene;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Base64;

/**
 * <h1>Controller</h1>
 *
 * @author Hamm.cn
 */
@ApiController("openApp")
@Description("开放应用")
@Extends(exclude = {Api.Export, Api.QueryExport})
public class OpenAppController extends BaseController<OpenAppEntity, OpenAppService, OpenAppRepository> implements IOpenAppAction {
    @Autowired
    private NotifyService notifyService;

    @Description("通过AppKey获取应用信息")
    @PostMapping("getByAppKey")
    @Permission(login = false)
    @Filter(RootEntity.WhenGetDetail.class)
    public Json getByAppKey(@RequestBody @Validated(WhenGetByAppKey.class) OpenAppEntity openApp) {
        openApp = service.getByAppKey(openApp.getAppKey());
        ServiceError.DATA_NOT_FOUND.whenNull(openApp, "没有查到指定AppKey的应用");
        return Json.data(openApp);
    }

    @Override
    public Json add(@RequestBody @Validated(WhenAdd.class) @NotNull OpenAppEntity openApp) {
        openApp.setAppKey(service.createAppKey())
                .setAppSecret(service.createAppSecret())
        ;
        service.resetKeyPare(openApp);
        openApp = service.get(service.add(openApp));
        return Json.data(String.format("应用名称: %s\n\nAppKey:\n%s\n\nAppSecret:\n%s\n\n公钥:\n%s", openApp.getAppName(), openApp.getAppKey(), openApp.getAppSecret(), openApp.getPublicKey()));
    }

    @Description("重置密钥")
    @PostMapping("resetSecret")
    public Json resetSecret(@RequestBody @Validated(WhenIdRequired.class) OpenAppEntity openApp) {
        OpenAppEntity exist = service.get(openApp.getId());
        String appSecret = Base64.getEncoder().encodeToString(RandomUtil.randomBytes());
        exist.setAppSecret(appSecret);
        service.update(exist);
        notifyService.sendNotification(NotifyScene.APP_SECRET_RESET, exist, String.format(
                "应用 %s 的秘钥被重置", exist.getAppName())
        );
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
