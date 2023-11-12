package com.qqlab.spms.module.channel.sale;

import cn.hamm.airpower.interfaces.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>采购单状态</h1>
 *
 * @author Hamm
 */
@AllArgsConstructor
public enum SaleStatus implements IEnum {
    /**
     * <h2>审核中</h2>
     */
    AUDITING(1, "审核中"),

    /**
     * <h2>已驳回</h2>
     */
    REJECTED(2, "已驳回"),

    /**
     * <h2>出库中</h2>
     */
    OUTPUTING(3, "出库中"),

    /**
     * <h2>已完成</h2>
     */
    DONE(4, "已完成"),

    /**
     * <h2>已取消</h2>
     */
    CANCELED(5, "已取消");


    @Getter
    private int value;

    @Getter
    private String label;
}
