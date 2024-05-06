package cn.hamm.spms.base;

import cn.hamm.airpower.annotation.Permission;
import cn.hamm.airpower.model.json.JsonData;
import cn.hamm.airpower.model.query.QueryPageRequest;
import cn.hamm.airpower.model.query.QueryRequest;
import cn.hamm.airpower.root.RootEntityController;
import cn.hamm.spms.common.annotation.DisableLog;
import org.jetbrains.annotations.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * <h1>实体控制器基类</h1>
 *
 * @param <S> Service
 * @param <E> 实体
 * @param <R> 数据源
 * @author Hamm.cn
 */
@Permission
public class BaseController<E extends BaseEntity<E>, S extends BaseService<E, R>, R extends BaseRepository<E>> extends RootEntityController<E, S, R> {
    @DisableLog
    @Override
    public JsonData getList(@RequestBody QueryRequest<E> queryRequest) {
        return super.getList(queryRequest);
    }

    @DisableLog
    @Override
    public JsonData getPage(@RequestBody QueryPageRequest<E> queryPageRequest) {
        return super.getPage(queryPageRequest);
    }

    @DisableLog
    @Override
    public JsonData getDetail(@RequestBody @Validated(WhenIdRequired.class) @NotNull E entity) {
        return super.getDetail(entity);
    }
}