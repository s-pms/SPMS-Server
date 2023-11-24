package com.qqlab.spms.module.wms.input;

import cn.hamm.airpower.interfaces.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>入库状态</h1>
 *
 * @author Hamm
 */
@AllArgsConstructor
public enum InputStatus implements IEnum {
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


    @Getter
    private final int value;

    @Getter
    private final String label;
}
