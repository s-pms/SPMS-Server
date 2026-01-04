package cn.hamm.spms.module.asset.contract.enums;

import cn.hamm.airpower.core.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>参与人身份枚举</h1>
 *
 * @author Hamm.cn
 */
@AllArgsConstructor
@Getter
public enum ParticipantRole implements IDictionary {
    /**
     * 甲方
     */
    A(1, "甲方"),

    /**
     * 乙方
     */
    B(2, "乙方"),

    /**
     * 丙方
     */
    C(3, "丙方"),

    /**
     * 丁方
     */
    D(4, "丁方"),
    ;

    private final int key;
    private final String label;
}
