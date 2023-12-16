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
@Getter
public enum OrderType implements IEnum {
    /**
     * 计划订单
     */
    PLAN(1, "计划订单"),

    /**
     * 其他订单
     */
    OTHER(2, "其他订单");

    private final int value;
    private final String label;
}
