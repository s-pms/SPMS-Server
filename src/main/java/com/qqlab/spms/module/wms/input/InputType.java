package com.qqlab.spms.module.wms.input;

import cn.hamm.airpower.interfaces.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>入库类型</h1>
 *
 * @author Hamm
 */
@AllArgsConstructor
public enum InputType implements IEnum {
    /**
     * <h2>普通入库</h2>
     *
     * @noinspection unused
     */
    NORMAL(1, "普通入库"),

    /**
     * <h2>转移入库</h2>
     */
    MOVE(2, "转移入库"),

    /**
     * <h2>采购入库</h2>
     */
    PURCHASE(3, "采购入库"),
    ;


    @Getter
    private final int value;

    @Getter
    private final String label;
}
