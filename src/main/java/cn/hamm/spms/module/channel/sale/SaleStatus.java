package cn.hamm.spms.module.channel.sale;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>采购单状态</h1>
 *
 * @author Hamm.cn
 */
@AllArgsConstructor
@Getter
public enum SaleStatus implements IDictionary {
    /**
     * <h3>审核中</h3>
     */
    AUDITING(1, "审核中"),

    /**
     * <h3>已驳回</h3>
     */
    REJECTED(2, "已驳回"),

    /**
     * <h3>出库中</h3>
     */
    OUTPUTTING(3, "出库中"),

    /**
     * <h3>已完成</h3>
     */
    DONE(4, "已完成");

    private final int key;
    private final String label;
}
