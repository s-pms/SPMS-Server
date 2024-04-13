package cn.hamm.spms.module.mes.plan;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>生产计划状态</h1>
 *
 * @author Hamm
 */
@AllArgsConstructor
@Getter
public enum PlanStatus implements IDictionary {
    /**
     * <h2>审核中</h2>
     */
    AUDITING(1, "审核中"),

    /**
     * <h2>已驳回</h2>
     */
    REJECTED(2, "已驳回"),

    /**
     * <h2>生产中</h2>
     */
    PRODUCING(3, "生产中"),

    /**
     * <h2>已完成</h2>
     */
    DONE(4, "已完成");


    private final int key;
    private final String label;
}
