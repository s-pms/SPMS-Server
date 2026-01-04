package cn.hamm.spms.module.wms.output.enums;

import cn.hamm.airpower.core.interfaces.IDictionary;
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
     * 其他出库
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

    /**
     * 领料出库
     */
    PICKING(4, "领料出库");

    private final int key;
    private final String label;
}
