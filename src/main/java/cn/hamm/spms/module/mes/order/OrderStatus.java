package cn.hamm.spms.module.mes.order;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>订单状态</h1>
 *
 * @author Hamm.cn
 */
@AllArgsConstructor
@Getter
public enum OrderStatus implements IDictionary {
    /**
     * <h3>审核中</h3>
     */
    AUDITING(1, "审核中"),

    /**
     * <h3>已驳回</h3>
     */
    REJECTED(2, "已驳回"),

    /**
     * <h3>准备中</h3>
     */
    PREPARE(3, "准备中"),

    /**
     * <h3>生产中</h3>
     */
    PRODUCING(4, "生产中"),

    /**
     * <h3>入库中</h3>
     */
    INPUTTING(5, "入库中"),

    /**
     * <h3>已完成</h3>
     */
    DONE(6, "已完成"),

    /**
     * <h3>暂停中</h3>
     */
    PAUSED(7, "暂停中");

    private final int key;
    private final String label;
}
