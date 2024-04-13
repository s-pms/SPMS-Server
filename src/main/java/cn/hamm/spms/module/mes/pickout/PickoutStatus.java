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
     * <h2>审核中</h2>
     */
    AUDITING(1, "审核中"),

    /**
     * <h2>已驳回</h2>
     */
    REJECTED(2, "已驳回"),

    /**
     * <h2>出库中</h2>
     */
    PURCHASING(3, "出库中"),

    /**
     * <h2>已完成</h2>
     */
    DONE(4, "已完成"),

    /**
     * <h2>已取消</h2>
     */
    CANCELED(5, "已取消");

    private final int key;
    private final String label;
}
