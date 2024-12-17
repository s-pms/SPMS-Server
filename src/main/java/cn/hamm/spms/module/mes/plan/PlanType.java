package cn.hamm.spms.module.mes.plan;

import cn.hamm.airpower.interfaces.IDictionary;
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
     * <h3>内部计划</h3>
     */
    INNER(1, "内部计划"),

    /**
     * <h3>外销计划</h3>
     */
    SALE(2, "外销计划");

    private final int key;
    private final String label;
}
