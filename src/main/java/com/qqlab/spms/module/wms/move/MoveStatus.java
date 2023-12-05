package com.qqlab.spms.module.wms.move;

import cn.hamm.airpower.interfaces.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>移库状态</h1>
 *
 * @author Hamm
 */
@AllArgsConstructor
public enum MoveStatus implements IEnum {
    /**
     * 审核中
     */
    AUDITING(1, "审核中"),

    /**
     * 已驳回
     */
    REJECTED(2, "已驳回"),

    /**
     * 移动中
     */
    MOVING(3, "移动中"),

    /**
     * 已完成
     */
    DONE(4, "已完成");


    @Getter
    private final int value;

    @Getter
    private final String label;
}
