package cn.hamm.spms.module.iot.report;

import cn.hamm.airpower.core.dictionary.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>数据类型</h1>
 *
 * @author Hamm.cn
 */
@AllArgsConstructor
@Getter
public enum ReportDataType implements IDictionary {
    /**
     * <h3>数量</h3>
     */
    NUMBER(1, "数量"),

    /**
     * <h3>状态</h3>
     */
    STATUS(2, "状态"),

    /**
     * <h3>开关</h3>
     */
    BOOLEAN(3, "开关"),

    /**
     * <h3>信息</h3>
     */
    STRING(4, "信息"),
    ;
    private final int key;
    private final String label;
}
