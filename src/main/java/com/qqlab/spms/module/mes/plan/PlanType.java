package com.qqlab.spms.module.mes.plan;

import cn.hamm.airpower.interfaces.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>生产计划类型</h1>
 *
 * @author Hamm
 */
@AllArgsConstructor
public enum PlanType implements IEnum {
    /**
     * 内部计划
     */
    INNER(1, "内部计划"),

    /**
     * 外销计划
     */
    SALE(2, "外销计划");


    @Getter
    private final int value;

    @Getter
    private final String label;
}
