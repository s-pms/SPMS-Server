package cn.hamm.spms.module.iot.parameter.enums;

import cn.hamm.airpower.core.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>参数数据类型</h1>
 *
 * @author Hamm.cn
 * @apiNote 当前 {@code ParameterEntity#dataType} 实际绑定的是 {@code ReportDataType}，
 * 本枚举暂无引用方
 */
@Getter
@AllArgsConstructor
public enum ParameterType implements IDictionary {
    /**
     * 数字：可参与求和、平均等统计
     */
    NUMBER(1, "数字"),

    /**
     * 布尔：仅 true / false 两种取值
     */
    BOOLEAN(2, "布尔"),

    /**
     * 字符串：原样存储，不做类型转换
     */
    STRING(3, "字符串"),

    ;

    private final int key;
    private final String label;
}
