package cn.hamm.spms.module.system.config.enums;

import cn.hamm.airpower.web.dictionary.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>系统配置类型</h1>
 *
 * @author Hamm.cn
 */
@Getter
@AllArgsConstructor
public enum ConfigType implements IDictionary {
    /**
     * 字符串类型
     */
    STRING(0, "字符串类型"),

    /**
     * 布尔类型
     */
    BOOLEAN(1, "布尔类型"),

    /**
     * 数字类型
     */
    NUMBER(2, "数字类型"),
    ;

    private final int key;
    private final String label;
}
