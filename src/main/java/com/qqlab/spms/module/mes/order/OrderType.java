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
     * 计划订单
     */
    PLAN(1, "计划订单"),

    /**
     * 其他订单
     */
    OTHER(2, "其他订单");


    @Getter
    private final int value;

    @Getter
    private final String label;
}
