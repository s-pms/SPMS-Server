package com.qqlab.spms.module.iot.parameter;

import cn.hamm.airpower.interfaces.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>数据类型</h1>
 *
 * @author Hamm
 */
@Getter
@AllArgsConstructor
public enum ParameterType implements IEnum {
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

    private final int value;
    private final String label;
}
