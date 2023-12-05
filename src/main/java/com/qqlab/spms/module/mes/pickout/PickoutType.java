package com.qqlab.spms.module.mes.pickout;

import cn.hamm.airpower.interfaces.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>领料单类型</h1>
 *
 * @author Hamm
 */
@AllArgsConstructor
public enum PickoutType implements IEnum {
    /**
     * 生产领料
     */
    PRODUCE(1, "生产领料"),

    /**
     * 其他领料
     */
    OTHER(2, "其他领料");


    @Getter
    private final int value;

    @Getter
    private final String label;
}
