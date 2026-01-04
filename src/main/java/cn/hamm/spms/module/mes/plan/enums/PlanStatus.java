package cn.hamm.spms.module.mes.plan.enums;

import cn.hamm.airpower.core.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>生产计划状态</h1>
 *
 * @author Hamm.cn
 */
@AllArgsConstructor
@Getter
public enum PlanStatus implements IDictionary {
    /**
     * 审核中
     */
    AUDITING(1, "审核中"),

    /**
     * 已驳回
     */
    REJECTED(2, "已驳回"),

    /**
     * 生产中
     */
    PRODUCING(3, "生产中"),

    /**
     * 已完成
     */
    DONE(4, "已完成");

    private final int key;
    private final String label;
}
