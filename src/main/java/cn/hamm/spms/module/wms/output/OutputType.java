package cn.hamm.spms.module.wms.output;

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
     * <h2>其他出库</h2>
     */
    NORMAL(1, "普通出库"),

    /**
     * <h2>转移出库</h2>
     */
    MOVE(2, "转移出库"),

    /**
     * <h2>销售出库</h2>
     */
    SALE(3, "销售出库"),
    ;

    private final int key;
    private final String label;
}
