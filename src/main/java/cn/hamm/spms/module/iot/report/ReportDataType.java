package cn.hamm.spms.module.iot.report;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>数据类型</h1>
 *
 * @author Hamm
 */
@AllArgsConstructor
@Getter
public enum ReportDataType implements IDictionary {
    /**
     * <h2>数量</h2>
     */
    QUANTITY(1, "数量"),

    /**
     * <h2>状态</h2>
     */
    STATUS(2, "状态"),

    /**
     * <h2>开关</h2>
     */
    SWITCH(3, "开关"),

    /**
     * <h2>信息</h2>
     */
    INFORMATION(4, "信息"),
    ;
    private final int key;
    private final String label;
}
