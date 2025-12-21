package cn.hamm.spms.base;

import cn.hamm.airpower.curd.CurdService;
import cn.hamm.airpower.reflect.ReflectUtil;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.common.annotation.AutoGenerateCode;
import cn.hamm.spms.module.system.coderule.CodeRuleService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

/**
 * <h1>基础服务类</h1>
 *
 * @param <E> 实体
 * @param <R> 数据源
 * @author Hamm.cn
 */
@Slf4j
public class BaseService<E extends BaseEntity<E>, R extends BaseRepository<E>> extends CurdService<E, R> {
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
        List<Field> fields = ReflectUtil.getFieldList(entity.getClass());
        CodeRuleService codeRuleService = Services.getCodeRuleService();
        for (Field field : fields) {
            AutoGenerateCode autoGenerateCode = ReflectUtil.getAnnotation(AutoGenerateCode.class, field);
            if (Objects.isNull(autoGenerateCode)) {
                continue;
            }
            Object value = ReflectUtil.getFieldValue(entity, field);
            if (!Objects.isNull(value) && StringUtils.hasText(value.toString())) {
                continue;
            }
            String code = codeRuleService.createCode(autoGenerateCode.value());
            ReflectUtil.setFieldValue(entity, field, code);
            break;
        }
        return beforeAppSaveToDatabase(entity);
    }

    /**
     * 发布
     *
     * @param id ID
     */
    public final void publish(Long id) {
        E exist = get(id);
        beforePublish(exist.copy());
        exist.setIsPublished(true);
        update(exist);
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
    protected final void afterAdd(@NotNull E entity, @NotNull E source) {
        afterAppAdd(entity, source);
    }

    protected void afterAppAdd(@NotNull E entity, @NotNull E source) {
    }

    @Override
    protected void afterUpdate(@NotNull E entity, @NotNull E source) {
        afterAppUpdate(entity, source);
    }

    protected void afterAppUpdate(@NotNull E entity, @NotNull E source) {
    }
}
