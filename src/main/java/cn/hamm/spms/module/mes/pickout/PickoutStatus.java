package cn.hamm.spms.module.mes.pickout;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>领料单状态</h1>
 *
 * @author Hamm
 */
@AllArgsConstructor
@Getter
public enum PickoutStatus implements IDictionary {
    /**
     * 审核中
     */
    AUDITING(1, "审核中"),

    /**
     * 已驳回
     */
    REJECTED(2, "已驳回"),

    /**
     * 出库中
     */
    PURCHASING(3, "出库中"),

    /**
     * 已完成
     */
    DONE(4, "已完成"),

    /**
     * 已取消
     */
    CANCELED(5, "已取消");

    private final int key;
    private final String label;
}