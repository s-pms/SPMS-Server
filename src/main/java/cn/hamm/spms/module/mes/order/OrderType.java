package cn.hamm.spms.module.mes.order;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>订单类型</h1>
 *
 * @author Hamm.cn
 */
@AllArgsConstructor
@Getter
public enum OrderType implements IDictionary {
    /**
     * <h3>计划订单</h3>
     */
    PLAN(1, "计划订单"),

    /**
     * <h3>其他订单</h3>
     */
    OTHER(2, "其他订单");

    private final int key;
    private final String label;
}
