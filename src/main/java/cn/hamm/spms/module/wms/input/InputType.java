package cn.hamm.spms.module.wms.input;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>入库类型</h1>
 *
 * @author Hamm.cn
 */
@AllArgsConstructor
@Getter
public enum InputType implements IDictionary {
    /**
     * <h3>普通入库</h3>
     */
    NORMAL(1, "普通入库"),

    /**
     * <h3>转移入库</h3>
     */
    MOVE(2, "转移入库"),

    /**
     * <h3>采购入库</h3>
     */
    PURCHASE(3, "采购入库"),

    /**
     * <h3>生产入库</h3>
     */
    PRODUCTION(4, "生产入库"),
    ;

    private final int key;
    private final String label;
}
