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
    AUDITING(1, "审核中"),

    REJECTED(2, "已驳回"),

    PRODUCING(3, "生产中"),

    DONE(4, "已完成");

    private final int key;
    private final String label;
}
