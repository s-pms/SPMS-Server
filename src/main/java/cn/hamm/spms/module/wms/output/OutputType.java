package cn.hamm.spms.module.wms.output;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>出库类型</h1>
 *
 * @author Hamm.cn
 */
@AllArgsConstructor
@Getter
public enum OutputType implements IDictionary {
    /**
     * <h3>其他出库</h3>
     */
    NORMAL(1, "普通出库"),

    /**
     * <h3>转移出库</h3>
     */
    MOVE(2, "转移出库"),

    /**
     * <h3>销售出库</h3>
     */
    SALE(3, "销售出库"),
    ;

    private final int key;
    private final String label;
}
