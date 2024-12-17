package cn.hamm.spms.module.mes.bom;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>BOM状态</h1>
 *
 * @author Hamm.cn
 */
@AllArgsConstructor
@Getter
public enum BomStatus implements IDictionary {
    /**
     * <h3>审核中</h3>
     */
    AUDITING(1, "审核中"),

    /**
     * <h3>已驳回</h3>
     */
    REJECTED(2, "已驳回"),

    /**
     * <h3>已发布</h3>
     */
    PUBLISHED(3, "已发布");

    private final int key;
    private final String label;
}
