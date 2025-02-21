package cn.hamm.spms.module.system.config;

import cn.hamm.airpower.config.Constant;
import cn.hamm.airpower.exception.ServiceError;
import cn.hamm.airpower.util.DictionaryUtil;
import cn.hamm.spms.base.BaseService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

/**
 * <h1>Service</h1>
 *
 * @author Hamm.cn
 */
@Service
public class ConfigService extends BaseService<ConfigEntity, ConfigRepository> {
    /**
     * <h3>根据配置枚举获取配置信息</h3>
     *
     * @param configFlag 配置枚举
     * @return 配置信息
     */
    public final ConfigEntity get(@NotNull ConfigFlag configFlag) {
        return get(configFlag.name());
    }

    /**
     * <h3>根据配置标识获取配置信息</h3>
     *
     * @param flag 配置标识
     * @return 配置信息
     */
    public final ConfigEntity get(@NotNull String flag) {
        ConfigEntity configuration = repository.getByFlag(flag);
        ServiceError.DATA_NOT_FOUND.whenNull(configuration, "查询配置失败");
        return configuration;
    }

    @Override
    protected void beforeDelete(long id) {
        ConfigEntity config = get(id);
        ServiceError.FORBIDDEN_DELETE.when(config.getIsSystem(), "系统内置配置无法被删除!");
    }

    @Override
    protected ConfigEntity beforeAppSaveToDatabase(@NotNull ConfigEntity configuration) {
        String key = configuration.getFlag();
        configuration.setIsSystem(false);
        // 如果 Configuration枚举中包含这个标识 则设置为系统标识
        for (ConfigFlag configFlag : ConfigFlag.values()) {
            if (configFlag.name().equals(key)) {
                configuration.setIsSystem(true);
                ConfigType type = DictionaryUtil.getDictionary(ConfigType.class, configuration.getType());
                switch (type) {
                    case BOOLEAN:
                        configuration.setConfig(
                                Constant.ONE_STRING.equals(configuration.getConfig()) ?
                                        Constant.ONE_STRING :
                                        Constant.ZERO_STRING
                        );
                        break;
                    case NUMBER:
                        configuration.setConfig(
                                Long.valueOf(configuration.getConfig()).toString()
                        );
                        break;
                    default:
                }
                break;
            }
        }
        return configuration;
    }
}
