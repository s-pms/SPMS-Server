package cn.hamm.spms.module.iot.report;

import cn.hamm.airpower.dictionary.IDictionary;
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
     * 数量
     */
    NUMBER(1, "数量"),

    /**
     * 状态
     */
    STATUS(2, "状态"),

    /**
     * 开关
     */
    BOOLEAN(3, "开关"),

    /**
     * 信息
     */
    STRING(4, "信息"),
    ;
    private final int key;
    private final String label;
}
