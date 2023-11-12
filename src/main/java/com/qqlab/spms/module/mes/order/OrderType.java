package com.qqlab.spms.module.mes.order;

import cn.hamm.airpower.interfaces.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>订单类型</h1>
 *
 * @author Hamm
 */
@AllArgsConstructor
public enum OrderType implements IEnum {
    /**
     * <h2>计划订单</h2>
     */
    PLAN(1, "计划订单"),

    /**
     * <h2>其他订单</h2>
     */
    OTHER(2, "其他订单");


    @Getter
    private int value;

    @Getter
    private String label;
}
