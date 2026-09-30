package cn.hamm.spms.base;

import cn.hamm.airpower.curd.base.CurdService;
import cn.hamm.spms.module.system.SystemServices;
import cn.hamm.spms.module.system.coderule.CodeRuleService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;


/**
 * <h1>基础服务类</h1>
 * <p>
 * <b>删除一律走 {@code delete(long id)}</b>，它会触发
 * {@code beforeDelete} / {@code afterDelete} 钩子。
 * </p>
 * <p>
 * 不要用 {@code repository.deleteAll(...)} + {@code repository.flush()}：
 * 那是直接拼的批量 SQL，不触发 JPA 实体生命周期回调、不走钩子、不做级联，
 * 实体上若挂了需要清理的关联会静默留下脏数据。
 * </p>
 * <p>
 * 也不要写 {@code ids.forEach(this::delete)}。
 * 这个写法曾经会导致「删 N 条只删掉最后 1 条」：
 * {@code delete(long id)} 第一步是 {@code get(id)}，而当时的
 * {@code CurdService.getById} 第一行是 {@code entityManager.clear()}，
 * 循环里每删一条都先 {@code get} 一次，{@code clear()} 把上一条已标记删除的
 * 实体从持久化上下文丢弃，那条删除再也不会执行。
 * 该 {@code clear()} 已从框架移除，此处保留说明以免后人重新引入。
 * </p>
 *
 * @param <E> 实体
 * @param <R> 数据源
 * @author Hamm.cn
 */
@Slf4j
public class BaseService<
        E extends BaseEntity<E>,
        R extends BaseRepository<E>
        > extends CurdService<E, R> {

    /**
     * 当前服务的数据库最后一次确认
     *
     * @param entity 实体
     * @return 处理后的数据
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
     * 发布
     *
     * @param id ID
     */
    public final void publish(long id) {
        transactionHelper.run(() -> {
            E entity = get(id);
            beforePublish(entity);
            updateToDatabase(getEntityInstance(id).setIsPublished(true));
        });
    }

    /**
     * 发布前
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
