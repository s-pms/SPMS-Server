package cn.hamm.spms.module.mes.pickout;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>领料单状态</h1>
 *
 * @author Hamm.cn
 */
@AllArgsConstructor
@Getter
public enum PickoutStatus implements IDictionary {
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
    PURCHASING(3, "出库中"),

    /**
     * <h3>已完成</h3>
     */
    DONE(4, "已完成"),

    /**
     * <h3>已取消</h3>
     */
    CANCELED(5, "已取消");

    private final int key;
    private final String label;
}
