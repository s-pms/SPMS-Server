package com.qqlab.spms.module.wms.output;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>出库类型</h1>
 *
 * @author Hamm
 */
@AllArgsConstructor
@Getter
public enum OutputType implements IDictionary {
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

    private final int key;
    private final String label;
}
