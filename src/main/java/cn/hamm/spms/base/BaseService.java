package cn.hamm.spms.base;

import cn.hamm.airpower.curd.base.CurdService;
import cn.hamm.spms.module.system.SystemServices;
import cn.hamm.spms.module.system.coderule.CodeRuleService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;


/**
 * <h1>实体服务基类</h1>
 *
 * @param <E> 实体
 * @param <R> 数据源
 * @author Hamm.cn
 * @apiNote 删除一律走 {@code delete(long id)}，它会触发 beforeDelete / afterDelete 钩子。
 * {@code repository.deleteAll} 这类批量 SQL 不走 JPA 生命周期回调，也不做级联
 */
@Slf4j
public class BaseService<
        E extends BaseEntity<E>,
        R extends BaseRepository<E>
        > extends CurdService<E, R> {

    /**
     * 入库前的最后一道处理
     *
     * @param entity 实体
     * @return 处理后的实体
     */
    protected E beforeAppSaveToDatabase(@NotNull E entity) {
        return entity;
    }

    @Override
    protected final @NotNull E beforeSaveToDatabase(@NotNull E entity) {
        CodeRuleService codeRuleService = SystemServices.getCodeRuleService();
        codeRuleService.fillFieldAutoCode(entity);
        return beforeAppSaveToDatabase(entity);
    }

    /**
     * 发布数据
     *
     * @param id 实体 ID
     * @apiNote 方法为 {@code final}，子类只能通过 {@link #beforePublish(BaseEntity)} 插入发布前逻辑
     */
    public final void publish(long id) {
        transactionHelper.run(() -> {
            E entity = get(id);
            beforePublish(entity);
            updateToDatabase(getEntityInstance(id).setIsPublished(true));
        });
    }

    /**
     * 发布前钩子
     *
     * @param entity 实体
     */
    protected void beforePublish(@NotNull E entity) {
        log.info("单据发布前，ID:{}", entity.getId());
    }

    @Override
    protected E afterGet(@NotNull E entity) {
        return afterAppGet(entity);
    }

    protected E afterAppGet(@NotNull E entity) {
        return entity;
    }

    @Override
    protected final void afterAdd(long id, @NotNull E source) {
        afterAppAdd(id, source);
    }

    protected void afterAppAdd(long id, @NotNull E source) {
    }

    @Override
    protected void afterUpdate(long id, @NotNull E source) {
        afterAppUpdate(id, source);
    }

    protected void afterAppUpdate(long id, @NotNull E source) {
    }
}
