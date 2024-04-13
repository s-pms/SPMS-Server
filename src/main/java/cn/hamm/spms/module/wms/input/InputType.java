package cn.hamm.spms.module.wms.input;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>入库类型</h1>
 *
 * @author Hamm
 */
@AllArgsConstructor
@Getter
public enum InputType implements IDictionary {
    /**
     * <h2>普通入库</h2>
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

    private final int key;
    private final String label;
}
