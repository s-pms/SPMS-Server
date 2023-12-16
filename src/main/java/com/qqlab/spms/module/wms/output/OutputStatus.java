package com.qqlab.spms.module.wms.output;

import cn.hamm.airpower.interfaces.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>出库状态</h1>
 *
 * @author Hamm
 */
@AllArgsConstructor
@Getter
public enum OutputStatus implements IEnum {
    /**
     * 审核中
     */
    AUDITING(1, "审核中"),

    /**
     * 已驳回
     */
    REJECTED(2, "已驳回"),

    /**
     * 出库中
     */
    OUTPUTTING(3, "出库中"),

    /**
     * 已完成
     */
    DONE(4, "已完成");

    private final int value;
    private final String label;
}
