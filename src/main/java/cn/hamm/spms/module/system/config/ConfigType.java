package cn.hamm.spms.module.system.config;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>系统配置类型</h1>
 *
 * @author Hamm.cn
 */
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
@Getter
@AllArgsConstructor
public enum ConfigType implements IDictionary {
    /**
     * <h3>字符串类型</h3>
     */
    STRING(0, "字符串类型"),

    /**
     * <h3>布尔类型</h3>
     */
    BOOLEAN(1, "布尔类型"),

    /**
     * <h3>数字类型</h3>
     */
    NUMBER(2, "数字类型"),
    ;

    private final int key;
    private final String label;
}
