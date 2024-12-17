package cn.hamm.spms.module.channel.purchase;

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
public enum PurchaseStatus implements IDictionary {
    /**
     * <h3>审核中</h3>
     */
    AUDITING(1, "审核中"),

    /**
     * <h3>已驳回</h3>
     */
    REJECTED(2, "已驳回"),

    /**
     * <h3>采购中</h3>
     */
    PURCHASING(3, "采购中"),

    /**
     * <h3>已完成</h3>
     */
    DONE(4, "已完成"),

    /**
     * <h3>已入库</h3>
     */
    FINISHED(5, "已入库");

    private final int key;
    private final String label;
}
