package cn.hamm.spms.module.system.config;

import cn.hamm.airpower.util.DictionaryUtil;
import cn.hamm.spms.base.BaseService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import static cn.hamm.airpower.config.Constant.STRING_ONE;
import static cn.hamm.airpower.config.Constant.STRING_ZERO;
import static cn.hamm.airpower.exception.ServiceError.DATA_NOT_FOUND;
import static cn.hamm.airpower.exception.ServiceError.FORBIDDEN_DELETE;

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
        DATA_NOT_FOUND.whenNull(configuration, "查询配置失败");
        return configuration;
    }

    @Override
    protected void beforeDelete(long id) {
        ConfigEntity config = get(id);
        FORBIDDEN_DELETE.when(config.getIsSystem(), "系统内置配置无法被删除!");
    }

    @Override
    protected ConfigEntity beforeAppSaveToDatabase(@NotNull ConfigEntity config) {
        String key = config.getFlag();
        config.setIsSystem(false);
        // 如果 Configuration枚举中包含这个标识 则设置为系统标识
        for (ConfigFlag configFlag : ConfigFlag.values()) {
            if (configFlag.name().equals(key)) {
                config.setIsSystem(true);
                ConfigType type = DictionaryUtil.getDictionary(ConfigType.class, config.getType());
                switch (type) {
                    case BOOLEAN:
                        config.setConfig(STRING_ONE.equals(config.getConfig()) ?
                                STRING_ONE :
                                STRING_ZERO
                        );
                        break;
                    case NUMBER:
                        config.setConfig(
                                Long.valueOf(config.getConfig()).toString()
                        );
                        break;
                    default:
                }
                break;
            }
        }
        return config;
    }
}
