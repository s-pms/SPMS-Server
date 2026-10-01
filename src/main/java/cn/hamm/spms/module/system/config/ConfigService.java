package cn.hamm.spms.module.system.config;

import cn.hamm.airpower.core.DictionaryUtil;
import cn.hamm.spms.base.BaseService;
import cn.hamm.spms.module.system.config.enums.ConfigFlag;
import cn.hamm.spms.module.system.config.enums.ConfigType;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import static cn.hamm.airpower.exception.Errors.DATA_NOT_FOUND;
import static cn.hamm.airpower.exception.Errors.FORBIDDEN_DELETE;

/**
 * <h1>系统配置</h1>
 *
 * @author Hamm.cn
 */
@Service
public class ConfigService extends BaseService<ConfigEntity, ConfigRepository> {
    /**
     * 布尔值「否」的存储形式
     */
    public static final String STRING_ZERO = "0";

    /**
     * 布尔值「是」的存储形式
     */
    public static final String STRING_ONE = "1";

    /**
     * 按配置枚举查询
     *
     * @param configFlag 配置枚举
     * @return 配置信息
     */
    public final ConfigEntity get(@NotNull ConfigFlag configFlag) {
        return get(configFlag.name());
    }

    /**
     * 按配置标识查询
     *
     * @param flag 配置标识
     * @return 配置信息
     * @apiNote 枚举里没有登记过的标识（如历史遗留数据）不存在时直接报错，
     * 不要静默返回默认值，那会让配置看起来生效了其实没生效
     */
    public final ConfigEntity get(@NotNull String flag) {
        ConfigEntity configuration = repository.getByFlag(flag);
        DATA_NOT_FOUND.whenNull(configuration, "查询配置失败");
        return configuration;
    }

    @Override
    protected void beforeAppDelete(@NotNull ConfigEntity config) {
        FORBIDDEN_DELETE.when(config.getIsSystem(), "系统内置配置无法被删除!");
    }

    @Override
    protected ConfigEntity beforeAppSaveToDatabase(@NotNull ConfigEntity config) {
        String key = config.getFlag();
        config.setIsSystem(false);
        // 标识登记在 ConfigFlag 中才算内置配置，顺带按类型规范化存进去的值
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
