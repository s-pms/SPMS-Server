package com.qqlab.spms.iot.payload;

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
public enum IPayloadType implements IEnum {
    /**
     * <h2>数字</h2>
     */
    NUMBER(1, "数字"),

    /**
     * <h2>布尔</h2>
     */
    BOOLEAN(2, "布尔"),

    /**
     * <h2>字符串</h2>
     */
    STRING(3, "字符串"),

    ;

    private final int value;
    private final String label;
}
