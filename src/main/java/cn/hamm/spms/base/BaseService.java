package cn.hamm.spms.base;

import cn.hamm.airpower.result.Result;
import cn.hamm.airpower.root.RootService;
import cn.hamm.airpower.util.ReflectUtil;
import cn.hamm.spms.common.Services;
import cn.hamm.spms.common.annotation.AutoGenerateCode;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

/**
 * <h1>基础服务类</h1>
 *
 * @param <E> 实体
 * @param <R> 数据源
 * @author Hamm
 */
public class BaseService<E extends BaseEntity<E>, R extends BaseRepository<E>> extends RootService<E, R> {
    /**
     * 当前服务的数据库最后一次确认
     *
     * @param entity 实体
     * @return 处理后的数据
     */
    protected E beforeAppSaveToDatabase(E entity) {
        return entity;
    }

    @Override
    protected final E beforeSaveToDatabase(E entity) {
        entity = beforeAppSaveToDatabase(entity);
        List<Field> fields = ReflectUtil.getFieldList(entity.getClass());
        for (Field field : fields) {
            AutoGenerateCode autoGenerateCode = field.getAnnotation(AutoGenerateCode.class);
            if (Objects.isNull(autoGenerateCode)) {
                continue;
            }
            field.setAccessible(true);
            try {
                if (Objects.isNull(field.get(entity))) {
                    String code = Services.getCodeRuleService().createCode(autoGenerateCode.value());
                    field.set(entity, code);
                    break;
                }
            } catch (IllegalAccessException e) {
                Result.ERROR.show("生成编码规则失败，反射字段出现异常");
            }
        }
        return entity;
    }
}
