package cn.hamm.spms.module.iot.parameter;

import cn.hamm.spms.base.BaseService;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * <h1>Service</h1>
 *
 * @author Hamm
 */
@Service
public class ParameterService extends BaseService<ParameterEntity, ParameterRepository> {
    /**
     * 缓存的Key前缀
     */
    private final String PARAM_CODE_CACHE_PREFIX = "parameter_code_";

    /**
     * 通过参数编码查询
     *
     * @param code 参数编码
     * @return 参数
     */
    public ParameterEntity getByCode(String code) {
        ParameterEntity parameterEntity = redisUtil.getEntity(PARAM_CODE_CACHE_PREFIX + code, new ParameterEntity());
        if (Objects.nonNull(parameterEntity)) {
            if (Objects.isNull(parameterEntity.getId())) {
                return null;
            }
            return parameterEntity;
        }
        parameterEntity = repository.getByCode(code);
        if (Objects.isNull(parameterEntity)) {
            parameterEntity = new ParameterEntity();
        }
        redisUtil.saveEntity(PARAM_CODE_CACHE_PREFIX + code, parameterEntity);
        if (Objects.isNull(parameterEntity.getId())) {
            return null;
        }
        return parameterEntity;
    }

    @Override
    protected ParameterEntity beforeSaveToDatabase(ParameterEntity entity) {
        redisUtil.del(PARAM_CODE_CACHE_PREFIX + entity.getCode());
        return super.beforeSaveToDatabase(entity);
    }
}
