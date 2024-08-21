package cn.hamm.spms.module.webhook.factory.supplier;

import cn.hamm.spms.module.channel.supplier.SupplierEntity;
import cn.hamm.spms.module.webhook.AbstractEventFactory;
import cn.hamm.spms.module.webhook.WebHookEntity;
import org.jetbrains.annotations.NotNull;

/**
 * <h1>应用密钥重置</h1>
 *
 * @author Hamm.cn
 */
public class SupplierAddEvent extends AbstractEventFactory<SupplierEntity> {

    /**
     * <h2>获取通知内容</h2>
     *
     * @param webHook 通知钩子
     * @return 准备的数据
     */
    @Override
    protected String getWebHookContent(@NotNull WebHookEntity webHook) {
        return String.format("修改了供应商 (%s) 的信息",
                getData().getName()
        );
    }
}
