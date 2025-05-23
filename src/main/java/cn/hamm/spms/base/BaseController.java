package cn.hamm.spms.base;

import cn.hamm.airpower.access.Permission;
import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.api.Json;
import cn.hamm.airpower.api.fiter.Filter;
import cn.hamm.airpower.curd.CurdController;
import cn.hamm.airpower.curd.query.QueryListRequest;
import cn.hamm.airpower.curd.query.QueryPageRequest;
import cn.hamm.spms.common.annotation.DisableLog;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static cn.hamm.airpower.exception.ServiceError.FORBIDDEN_EDIT;

/**
 * <h1>实体控制器基类</h1>
 *
 * @param <S> Service
 * @param <E> 实体
 * @param <R> 数据源
 * @author Hamm.cn
 */
@Slf4j
@Permission
public class BaseController<E extends BaseEntity<E>, S extends BaseService<E, R>, R extends BaseRepository<E>> extends CurdController<E, S, R> {
    @DisableLog
    @Override
    public Json getList(@RequestBody QueryListRequest<E> queryRequest) {
        return super.getList(queryRequest);
    }

    @DisableLog
    @Override
    public Json getPage(@RequestBody QueryPageRequest<E> queryPageRequest) {
        return super.getPage(queryPageRequest);
    }

    @DisableLog
    @Override
    public Json getDetail(@RequestBody @Validated(WhenIdRequired.class) @NotNull E entity) {
        return super.getDetail(entity);
    }

    @Override
    protected final E beforeUpdate(@NotNull E entity) {
        E exist = service.get(entity.getId());
        FORBIDDEN_EDIT.when(exist.getIsPublished(), "无法修改已经发布的数据");
        entity = beforeAppUpdate(entity);
        return entity;
    }

    protected E beforeAppUpdate(@NotNull E entity) {
        log.info("修改数据，ID:{}", entity.getId());
        return entity;
    }

    protected void beforeAppDelete(long id) {
        log.info("删除数据，ID:{}", id);
    }

    @Override
    protected final void beforeDelete(long id) {
        E entity = service.get(id);
        FORBIDDEN_EDIT.when(entity.getIsPublished(), "无法删除已经发布的数据");
        beforeAppDelete(id);
    }

    @Description("发布")
    @PostMapping("publish")
    @Filter(WhenGetDetail.class)
    public Json publish(@RequestBody @Validated(WhenIdRequired.class) E entity) {
        service.publish(entity.getId());
        return Json.success("发布成功");
    }
}
