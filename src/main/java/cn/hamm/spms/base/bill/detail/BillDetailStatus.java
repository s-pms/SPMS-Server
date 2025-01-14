package cn.hamm.spms.base.bill.detail;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>单据明细状态</h1>
 *
 * @author Hamm.cn
 */
@Getter
@AllArgsConstructor
public enum BillDetailStatus implements IDictionary {
    /**
     * <h3>待处理</h3>
     */
    WAITING(1, "待处理"),

    /**
     * <h3>处理中</h3>
     */
    PROCESSING(2, "处理中"),

    /**
     * <h3>已完成</h3>
     */
    FINISHED(3, "已完成");

    private final int key;
    private final String label;
}
