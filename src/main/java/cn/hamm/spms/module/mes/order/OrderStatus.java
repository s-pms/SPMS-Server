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
     * <h3>生产中</h3>
     */
    PRODUCING(3, "生产中"),

    /**
     * <h3>入库中</h3>
     */
    INPUTTING(4, "入库中"),

    /**
     * <h3>已完成</h3>
     */
    DONE(5, "已完成");

    private final int key;
    private final String label;
}
