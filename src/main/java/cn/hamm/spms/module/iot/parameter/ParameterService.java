package cn.hamm.spms.module.iot.parameter;

import cn.hamm.spms.base.BaseService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class ParameterService extends BaseService<ParameterEntity, ParameterRepository> {
    /**
     * 缓存的 Key 前缀
     */
    private final String PARAM_CODE_CACHE_PREFIX = "parameter_code_";

    /**
     * 通过参数编码查询
     *
     * @param code 参数编码
     * @return 参数
     */
    public ParameterEntity getByCode(String code) {
        ParameterEntity parameter = redisHelper.getEntity(PARAM_CODE_CACHE_PREFIX + code, ParameterEntity.class);
        if (Objects.nonNull(parameter)) {
            if (Objects.isNull(parameter.getId())) {
                return null;
            }
            return parameter;
        }
        parameter = repository.getByCode(code);
        if (Objects.isNull(parameter)) {
            parameter = new ParameterEntity();
        }
        redisHelper.saveEntity(PARAM_CODE_CACHE_PREFIX + code, parameter);
        if (Objects.isNull(parameter.getId())) {
            return null;
        }
        return parameter;
    }

    @Override
    protected ParameterEntity beforeAppSaveToDatabase(@NotNull ParameterEntity parameter) {
        redisHelper.delete(PARAM_CODE_CACHE_PREFIX + parameter.getCode());
        return parameter;
    }
}
