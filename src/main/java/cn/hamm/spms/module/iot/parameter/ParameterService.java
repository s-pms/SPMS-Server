package cn.hamm.spms.module.iot.parameter;

import cn.hamm.spms.base.BaseService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * <h1>采集参数</h1>
 *
 * @author Hamm.cn
 */
@Service
public class ParameterService extends BaseService<ParameterEntity, ParameterRepository> {
    /**
     * 参数编码缓存的 Key 前缀
     */
    private final String PARAM_CODE_CACHE_PREFIX = "parameter_code_";

    /**
     * 通过参数编码查询
     *
     * @param code 参数编码
     * @return 参数，不存在时返回 {@code null}
     * @apiNote 未命中时会把一个 {@code id} 为 {@code null} 的空实体写进缓存，靠 {@code getId()} 为
     * {@code null} 来表示「查过但不存在」，所以本方法返回 {@code null} 时缓存里一定有值。
     * {@code beforeAppSaveToDatabase} 负责在落库时清掉这条负缓存
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

    /**
     * 清理参数编码缓存
     *
     * @param parameter 待保存的参数
     * @return 参数
     * @apiNote 删除不挂钩子：{@code ParameterEntity} 无删除接口，且缓存有 TTL 可兜底
     */
    @Override
    protected ParameterEntity beforeAppSaveToDatabase(@NotNull ParameterEntity parameter) {
        redisHelper.delete(PARAM_CODE_CACHE_PREFIX + parameter.getCode());
        return parameter;
    }
}
