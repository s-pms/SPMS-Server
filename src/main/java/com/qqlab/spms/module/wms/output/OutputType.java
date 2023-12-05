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
     * 其他出库
     *
     * @noinspection unused
     */
    NORMAL(1, "普通出库"),

    /**
     * 转移出库
     */
    MOVE(2, "转移出库"),

    /**
     * 销售出库
     */
    SALE(3, "销售出库"),
    ;


    @Getter
    private final int value;

    @Getter
    private final String label;
}
