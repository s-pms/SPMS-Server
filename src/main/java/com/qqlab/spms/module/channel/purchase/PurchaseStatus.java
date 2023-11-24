package com.qqlab.spms.module.channel.purchase;

import cn.hamm.airpower.interfaces.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>采购单状态</h1>
 *
 * @author Hamm
 */
@AllArgsConstructor
public enum PurchaseStatus implements IEnum {
    /**
     * <h2>审核中</h2>
     */
    AUDITING(1, "审核中"),

    /**
     * <h2>已驳回</h2>
     */
    REJECTED(2, "已驳回"),

    /**
     * <h2>采购中</h2>
     */
    PURCHASING(3, "采购中"),

    /**
     * <h2>已完成</h2>
     */
    DONE(4, "已完成"),

    /**
     * <h2>已入库</h2>
     */
    FINISHED(5, "已入库");


    @Getter
    private final int value;

    @Getter
    private final String label;
}
