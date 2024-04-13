package cn.hamm.spms.module.wms.input;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>入库状态</h1>
 *
 * @author Hamm
 */
@AllArgsConstructor
@Getter
public enum InputStatus implements IDictionary {
    /**
     * <h2>审核中</h2>
     */
    AUDITING(1, "审核中"),

    /**
     * <h2>已驳回</h2>
     */
    REJECTED(2, "已驳回"),

    /**
     * <h2>入库中</h2>
     */
    INPUTTING(3, "入库中"),

    /**
     * <h2>已完成</h2>
     */
    DONE(4, "已完成");

    private final int key;
    private final String label;
}
