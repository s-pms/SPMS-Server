package cn.hamm.spms.module.channel.purchase;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>采购单状态</h1>
 *
 * @author Hamm
 */
@AllArgsConstructor
@Getter
public enum PurchaseStatus implements IDictionary {
    /**
     * 审核中
     */
    AUDITING(1, "审核中"),

    /**
     * 已驳回
     */
    REJECTED(2, "已驳回"),

    /**
     * 采购中
     */
    PURCHASING(3, "采购中"),

    /**
     * 已完成
     */
    DONE(4, "已完成"),

    /**
     * 已入库
     */
    FINISHED(5, "已入库");

    private final int key;
    private final String label;
}
