package cn.hamm.spms.base;

import cn.hamm.airpower.curd.base.CurdService;
import cn.hamm.spms.module.system.SystemServices;
import cn.hamm.spms.module.system.coderule.CodeRuleService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * <h1>基础服务类</h1>
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

    /**
     * 批量删除指定实体，逐条保留前后置钩子
     * <p>
     * <b>不要写成 {@code forEach(this::delete)}</b>，那样会静默漏删。
     * {@code CurdService.delete(long id)} 的第一步是 {@code get(id)}，
     * 而 {@code CurdService.getById} 的第一行是 {@code entityManager.clear()} ——
     * 循环里每删一条都会先 {@code get} 一次，{@code clear()} 把上一条
     * 已经标记为删除的实体从持久化上下文里丢弃，那条删除就再也不会执行。
     * 实测「批量删除 2 条，只删掉最后 1 条」，另一条永久残留。
     * </p>
     * <p>
     * 也<b>不要</b>用 {@code repository.deleteAll(...)} +
     * {@code repository.flush()}：那是直接拼批量 SQL，
     * <b>不触发</b> JPA 实体生命周期回调、<b>不走</b> {@code beforeDelete}
     * 与 {@code afterDelete} 钩子，将来给实体挂上需要清理的关联时会静默留下脏数据。
     * </p>
     * <p>
     * 本方法的做法：调用方已把实体查出来（通常来自 {@code filter} 或
     * {@code findByXxx}），这里只对已加载的实体逐个 {@code repository.delete}。
     * 该方法内部只做 {@code em.find} + {@code em.remove}，<b>不会</b>
     * {@code clear()} 持久化上下文，因此循环里所有删除标记都能保留到事务提交时执行。
     * 循环体内也刻意不调用任何 {@code get}，避免触发 auto-flush 打断删除标记。
     * </p>
     *
     * @param entities 待删除的实体集合
     */
    public final void deleteAll(@NotNull Collection<E> entities) {
        if (entities.isEmpty()) {
            return;
        }
        for (E entity : entities) {
            beforeDelete(entity);
            repository.delete(entity);
            afterDelete(entity.getId());
        }
        log.info("已删除 {} 条明细记录", entities.size());
    }
}
