package cn.hamm.spms.module.wms.output;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>出库状态</h1>
 *
 * @author Hamm.cn
 */
@AllArgsConstructor
@Getter
public enum OutputStatus implements IDictionary {
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
    OUTPUTTING(3, "出库中"),

    /**
     * <h2>已完成</h2>
     */
    DONE(4, "已完成");

    private final int key;
    private final String label;
}
