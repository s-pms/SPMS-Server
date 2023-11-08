package com.qqlab.spms.module.channel.purchase;

import cn.hamm.airpower.interfaces.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>采购单状态</h1>
 *
 * @author Hamm https://hamm.cn
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
     * <h2>入库中</h2>
     */
    INPUTING(4, "入库中"),

    /**
     * <h2>采购完成</h2>
     */
    DONE(5, "采购完成"),

    /**
     * <h2>采购取消</h2>
     */
    CANCELED(6, "采购取消");


    @Getter
    private int value;

    @Getter
    private String label;
}
