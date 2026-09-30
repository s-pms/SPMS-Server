package cn.hamm.spms.module.mes.order.enums;

import cn.hamm.airpower.core.interfaces.IDictionary;
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
    AUDITING(1, "审核中"),

    REJECTED(2, "已驳回"),

    PREPARE(3, "准备中"),

    PRODUCING(4, "生产中"),

    INPUTTING(5, "入库中"),

    DONE(6, "已完成"),

    PAUSED(7, "暂停中");

    private final int key;
    private final String label;
}
