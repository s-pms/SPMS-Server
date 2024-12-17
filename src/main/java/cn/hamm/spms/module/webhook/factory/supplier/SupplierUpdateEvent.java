package cn.hamm.spms.module.webhook.factory.supplier;

import cn.hamm.spms.module.channel.supplier.SupplierEntity;
import cn.hamm.spms.module.webhook.AbstractEventFactory;
import cn.hamm.spms.module.webhook.WebHookEntity;
import org.jetbrains.annotations.NotNull;

/**
 * <h1>供应商更新</h1>
 *
 * @author Hamm.cn
 */
public class SupplierUpdateEvent extends AbstractEventFactory<SupplierEntity> {

    /**
     * <h3>获取通知内容</h3>
     *
     * @param webHook 通知钩子
     * @return 准备的数据
     */
    @Override
    protected String getWebHookContent(@NotNull WebHookEntity webHook) {
        return String.format("创建了一个新的供应商 (%s)",
                getData().getName()
        );
    }

    @Override
    protected Object prepareWebHookData(WebHookEntity webHook) {
        return getData();
    }
}
