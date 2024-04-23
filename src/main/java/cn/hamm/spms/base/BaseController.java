package cn.hamm.spms.base;

import cn.hamm.airpower.annotation.Description;
import cn.hamm.airpower.query.QueryPageRequest;
import cn.hamm.airpower.query.QueryRequest;
import cn.hamm.airpower.response.Filter;
import cn.hamm.airpower.result.json.JsonData;
import cn.hamm.airpower.root.RootEntityController;
import cn.hamm.airpower.security.Permission;
import cn.hamm.spms.common.annotation.LogDisabled;
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
    @LogDisabled
    @Override
    @Description("不分页查询")
    @Filter(WhenGetList.class)
    public JsonData getList(@RequestBody QueryRequest<E> queryRequest) {
        return super.getList(queryRequest);
    }

    @LogDisabled
    @Override
    @Description("分页查询")
    @Filter(WhenGetPage.class)
    public JsonData getPage(@RequestBody QueryPageRequest<E> queryPageRequest) {
        return super.getPage(queryPageRequest);
    }

    @LogDisabled
    @Override
    @Description("查看详情")
    @Filter(WhenGetDetail.class)
    public JsonData getDetail(@RequestBody @Validated(WhenIdRequired.class) E entity) {
        return super.getDetail(entity);
    }
}