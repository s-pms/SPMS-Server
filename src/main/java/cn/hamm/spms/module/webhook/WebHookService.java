package cn.hamm.spms.module.webhook;

import cn.hamm.airpower.exception.ServiceException;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.webhook.enums.WebHookScene;
import cn.hamm.spms.module.webhook.factory.open.AppSecretResetEvent;
import cn.hamm.spms.module.webhook.factory.supplier.SupplierAddEvent;
import cn.hamm.spms.module.webhook.factory.supplier.SupplierUpdateEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Slf4j
@Service
public class WebHookService extends BaseService<WebHookEntity, WebHookRepository> {
    /**
     * <h2>工厂列表</h2>
     */
    private final Map<WebHookScene, AbstractEventFactory<?>> factoryMap = Map.of(
            WebHookScene.APP_SECRET_RESET, new AppSecretResetEvent(),
            WebHookScene.SUPPLIER_ADD, new SupplierAddEvent(),
            WebHookScene.SUPPLIER_UPDATE, new SupplierUpdateEvent()
    );

    /**
     * <h2>线程池</h2>
     */
    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(
            5,
            20,
            3600L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>()
    );

    /**
     * <h2>发送通知</h2>
     *
     * @param scene  场景
     * @param entity 实体
     */
    public <E> void sendHook(WebHookScene scene, E entity) {
        try {
            EXECUTOR.submit(() -> getFactory(scene, entity).request());
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
        }
    }

    /**
     * <h2>获取工厂</h2>
     *
     * @param scene 场景
     * @return 工厂
     */
    @SuppressWarnings("unchecked")
    private <E, F extends AbstractEventFactory<E>> @NotNull F getFactory(@NotNull WebHookScene scene, E entity) {
        try {
            F factory = (F) factoryMap.get(scene);
            factory.setData(entity).setScene(scene);
            return factory;
        } catch (Exception exception) {
            throw new ServiceException("没有找到对应的通知工厂");
        }
    }
}
