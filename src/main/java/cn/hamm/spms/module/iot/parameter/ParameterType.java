package cn.hamm.spms.module.iot.parameter;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>数据类型</h1>
 *
 * @author Hamm.cn
 */
@Getter
@AllArgsConstructor
public enum ParameterType implements IDictionary {
    /**
     * <h3>数字</h3>
     */
    NUMBER(1, "数字"),

    /**
     * <h3>布尔</h3>
     */
    BOOLEAN(2, "布尔"),

    /**
     * <h3>字符串</h3>
     */
    STRING(3, "字符串"),

    ;

    private final int key;
    private final String label;
}
