package cn.hamm.spms.base;

import cn.hamm.airpower.root.RootService;
import cn.hamm.airpower.util.ReflectUtil;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.common.annotation.AutoGenerateCode;
import cn.hamm.spms.module.system.coderule.CodeRuleService;
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
public class BaseService<E extends BaseEntity<E>, R extends BaseRepository<E>> extends RootService<E, R> {
    /**
     * <h3>当前服务的数据库最后一次确认</h3>
     *
     * @param entity 实体
     * @return 处理后的数据
     */
    protected E beforeAppSaveToDatabase(E entity) {
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
        entity = beforeAppSaveToDatabase(entity);
        return entity;
    }

    /**
     * <h2>发布</h2>
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
     * <h2>发布前</h2>
     *
     * @param entity 实体
     */
    protected void beforePublish(@NotNull E entity) {
    }
}
