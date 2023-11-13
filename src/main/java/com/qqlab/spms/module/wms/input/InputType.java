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
     * <h2>采购入库</h2>
     */
    PURCHASE(1, "采购入库"),

    /**
     * <h2>退料入库</h2>
     */
    RESTORE(2, "退料入库"),

    /**
     * <h2>生产入库</h2>
     */
    PRODUCE(3, "生产入库"),

    /**
     * <h2>盘点入库</h2>
     */
    REVIEW(4, "盘点入库"),

    /**
     * <h2>转移入库</h2>
     */
    MOVE(5, "转移入库"),

    /**
     * <h2>其他入库</h2>
     */
    OTHER(6, "其他入库");


    @Getter
    private int value;

    @Getter
    private String label;
}
