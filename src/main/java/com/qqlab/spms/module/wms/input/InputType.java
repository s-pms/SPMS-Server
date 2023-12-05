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
     * 普通入库
     *
     * @noinspection unused
     */
    NORMAL(1, "普通入库"),

    /**
     * 转移入库
     */
    MOVE(2, "转移入库"),

    /**
     * 采购入库
     */
    PURCHASE(3, "采购入库"),
    ;


    @Getter
    private final int value;

    @Getter
    private final String label;
}
