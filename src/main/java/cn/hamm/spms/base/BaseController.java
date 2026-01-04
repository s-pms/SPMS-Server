package cn.hamm.spms.base;

import cn.hamm.airpower.util.annotation.Description;
import cn.hamm.airpower.web.access.Permission;
import cn.hamm.airpower.web.api.Extends;
import cn.hamm.airpower.web.api.Json;
import cn.hamm.airpower.web.curd.Curd;
import cn.hamm.airpower.web.curd.CurdController;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static cn.hamm.airpower.web.exception.ServiceError.FORBIDDEN_EDIT;

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
@Extends(exclude = {Curd.Export, Curd.QueryExport, Curd.Disable, Curd.Enable})
public class BaseController<E extends BaseEntity<E>, S extends BaseService<E, R>, R extends BaseRepository<E>> extends CurdController<E, S, R> {
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

    protected void beforeAppDelete(@NotNull E entity) {
        log.info("删除数据，ID:{}", entity.getId());
    }

    @Override
    protected void beforeDelete(@NotNull E entity) {
        FORBIDDEN_EDIT.when(entity.getIsPublished(), "无法删除已经发布的数据");
        beforeAppDelete(entity);
    }

    @Description("发布")
    @PostMapping("publish")
    public Json publish(@RequestBody @Validated(WhenIdRequired.class) E entity) {
        service.publish(entity.getId());
        return Json.success("发布成功");
    }
}
