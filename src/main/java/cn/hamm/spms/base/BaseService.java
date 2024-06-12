package cn.hamm.spms.base;


import cn.hamm.airpower.root.RootService;
import cn.hamm.airpower.util.Utils;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.common.annotation.AutoGenerateCode;
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
     * <h2>当前服务的数据库最后一次确认</h2>
     *
     * @param entity 实体
     * @return 处理后的数据
     */
    protected E beforeAppSaveToDatabase(E entity) {
        return entity;
    }

    @Override
    protected final @NotNull E beforeSaveToDatabase(@NotNull E entity) {
        List<Field> fields = Utils.getReflectUtil().getFieldList(entity.getClass());
        for (Field field : fields) {
            AutoGenerateCode autoGenerateCode = Utils.getReflectUtil().getAnnotation(AutoGenerateCode.class, field);
            if (Objects.isNull(autoGenerateCode)) {
                continue;
            }
            Object value = Utils.getReflectUtil().getFieldValue(entity, field);
            if (!Objects.isNull(value) && StringUtils.hasText(value.toString())) {
                continue;
            }
            String code = Services.getCodeRuleService().createCode(autoGenerateCode.value());
            Utils.getReflectUtil().setFieldValue(entity, field, code);
            break;
        }
        entity = beforeAppSaveToDatabase(entity);
        return entity;
    }
}
