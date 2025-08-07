package cn.hamm.spms.module.mes.order.enums;

import cn.hamm.airpower.dictionary.IDictionary;
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
     * 审核中
     */
    AUDITING(1, "审核中"),

    /**
     * 已驳回
     */
    REJECTED(2, "已驳回"),

    /**
     * 准备中
     */
    PREPARE(3, "准备中"),

    /**
     * 生产中
     */
    PRODUCING(4, "生产中"),

    /**
     * 入库中
     */
    INPUTTING(5, "入库中"),

    /**
     * 已完成
     */
    DONE(6, "已完成"),

    /**
     * 暂停中
     */
    PAUSED(7, "暂停中");

    private final int key;
    private final String label;
}
