package cn.hamm.spms.module.system.config.enums;

import cn.hamm.airpower.core.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>配置值类型</h1>
 *
 * @author Hamm.cn
 */
@Getter
@AllArgsConstructor
public enum ConfigType implements IDictionary {
    /**
     * 任意字符串
     */
    STRING(0, "字符串类型"),

    /**
     * 布尔开关，存为 {@code "0"} / {@code "1"}
     */
    BOOLEAN(1, "布尔类型"),

    /**
     * 整数
     */
    NUMBER(2, "数字类型"),
    ;

    private final int key;
    private final String label;
}
