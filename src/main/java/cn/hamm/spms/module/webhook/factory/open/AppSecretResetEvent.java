package cn.hamm.spms.module.webhook.factory.open;

import cn.hamm.spms.module.open.app.OpenAppEntity;
import cn.hamm.spms.module.webhook.AbstractEventFactory;
import cn.hamm.spms.module.webhook.WebHookEntity;
import org.jetbrains.annotations.NotNull;

/**
 * <h1>应用密钥重置</h1>
 *
 * @author Hamm.cn
 */
public class AppSecretResetEvent extends AbstractEventFactory<OpenAppEntity> {

    /**
     * <h2>获取通知内容</h2>
     *
     * @param webHook 通知钩子
     * @return 准备的数据
     */
    @Override
    protected String getWebHookContent(@NotNull WebHookEntity webHook) {
        return String.format("应用 %s(%s) 密钥已被重置，如非主动操作，请及时重置并修改登录密码！",
                getData().getAppName(),
                getData().getAppKey()
        );
    }
}
