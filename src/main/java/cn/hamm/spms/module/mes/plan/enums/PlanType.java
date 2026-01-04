package cn.hamm.spms.module.mes.plan.enums;

import cn.hamm.airpower.core.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>生产计划类型</h1>
 *
 * @author Hamm.cn
 */
@AllArgsConstructor
@Getter
public enum PlanType implements IDictionary {
    /**
     * 内部计划
     */
    INNER(1, "内部计划"),

    /**
     * 外销计划
     */
    SALE(2, "外销计划");

    private final int key;
    private final String label;
}
