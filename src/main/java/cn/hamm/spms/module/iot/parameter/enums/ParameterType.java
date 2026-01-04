package cn.hamm.spms.module.iot.parameter.enums;

import cn.hamm.airpower.web.dictionary.IDictionary;
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
     * 数字
     */
    NUMBER(1, "数字"),

    /**
     * 布尔
     */
    BOOLEAN(2, "布尔"),

    /**
     * 字符串
     */
    STRING(3, "字符串"),

    ;

    private final int key;
    private final String label;
}
