package com.qqlab.spms.module.wms.output;

import cn.hamm.airpower.interfaces.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>出库类型</h1>
 *
 * @author Hamm
 */
@AllArgsConstructor
public enum OutputType implements IEnum {
    /**
     * <h2>销售出库</h2>
     */
    SALE(1, "销售出库"),

    /**
     * <h2>领料出库</h2>
     */
    PICKOUT(2, "领料出库"),

    /**
     * <h2>盘点出库</h2>
     */
    REVIEW(3, "盘点出库"),

    /**
     * <h2>转移出库</h2>
     */
    MOVE(4, "转移出库"),

    /**
     * <h2>其他出库</h2>
     */
    OTHER(5, "其他出库");


    @Getter
    private int value;

    @Getter
    private String label;
}
