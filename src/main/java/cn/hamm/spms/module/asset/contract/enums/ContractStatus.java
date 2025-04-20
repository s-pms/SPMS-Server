package cn.hamm.spms.module.asset.contract.enums;

import cn.hamm.airpower.dictionary.IDictionary;
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
     * 未生效
     */
    INVALID(1, "未生效"),

    /**
     * 生效中
     */
    EFFECTIVE(2, "生效中"),

    /**
     * 已终止
     */
    TERMINATED(3, "已终止");

    private final int key;
    private final String label;
}
