package cn.hamm.spms.module.wms.input.enums;

import cn.hamm.airpower.web.dictionary.IDictionary;
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
     * 普通入库
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

    /**
     * 生产入库
     */
    PRODUCTION(4, "生产入库"),
    ;

    private final int key;
    private final String label;
}
