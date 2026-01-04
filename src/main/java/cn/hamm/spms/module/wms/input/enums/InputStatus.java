package cn.hamm.spms.module.wms.input.enums;

import cn.hamm.airpower.core.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>入库状态</h1>
 *
 * @author Hamm.cn
 */
@AllArgsConstructor
@Getter
public enum InputStatus implements IDictionary {
    /**
     * 审核中
     */
    AUDITING(1, "审核中"),

    /**
     * 已驳回
     */
    REJECTED(2, "已驳回"),

    /**
     * 入库中
     */
    INPUTTING(3, "入库中"),

    /**
     * 已完成
     */
    DONE(4, "已完成");

    private final int key;
    private final String label;
}
