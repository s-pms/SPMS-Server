package com.qqlab.spms.module.mes.pickout;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>领料单类型</h1>
 *
 * @author Hamm
 */
@AllArgsConstructor
@Getter
public enum PickoutType implements IDictionary {
    /**
     * 生产领料
     */
    PRODUCE(1, "生产领料"),

    /**
     * 其他领料
     */
    OTHER(2, "其他领料");

    private final int key;
    private final String label;
}
