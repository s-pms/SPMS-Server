package cn.hamm.spms.module.asset.contract.enums;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>合同类型枚举</h1>
 *
 * @author Hamm.cn
 */
@AllArgsConstructor
@Getter
public enum ContractType implements IDictionary {
    /**
     * 其他合同
     */
    OTHER(0, "其他合同"),

    /**
     * 销售合同
     */
    SALE(1, "销售合同"),

    /**
     * 采购合同
     */
    PURCHASE(2, "采购合同"),

    /**
     * 劳动合同
     */
    LABOR(3, "劳动合同");

    private final int key;
    private final String label;
}
