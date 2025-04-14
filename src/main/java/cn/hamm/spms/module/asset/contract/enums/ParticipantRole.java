package cn.hamm.spms.module.asset.contract.enums;

import cn.hamm.airpower.interfaces.IDictionary;
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
     * <h3>甲方</h3>
     */
    A(1, "甲方"),

    /**
     * <h3>乙方</h3>
     */
    B(2, "乙方"),

    /**
     * <h3>丙方</h3>
     */
    C(3, "丙方"),

    /**
     * <h3>丁方</h3>
     */
    D(4, "丁方"),
    ;


    private final int key;
    private final String label;
}
