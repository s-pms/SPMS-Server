package cn.hamm.spms.module.asset.contract.enums;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>合同状态枚举</h1>
 *
 * @author Hamm.cn
 */
@AllArgsConstructor
@Getter
public enum ContractStatus implements IDictionary {
    /**
     * <h3>未生效</h3>
     */
    INVALID(1, "未生效"),

    /**
     * <h3>生效中</h3>
     */
    EFFECTIVE(2, "生效中"),

    /**
     * <h3>已终止</h3>
     */
    TERMINATED(3, "已终止");

    private final int key;
    private final String label;
}
