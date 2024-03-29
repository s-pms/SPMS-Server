package cn.hamm.spms.module.mes.bom;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>BOM状态</h1>
 *
 * @author Hamm
 */
@AllArgsConstructor
@Getter
public enum BomStatus implements IDictionary {
    /**
     * 审核中
     */
    AUDITING(1, "审核中"),

    /**
     * 已驳回
     */
    REJECTED(2, "已驳回"),

    /**
     * 已发布
     */
    PUBLISHED(3, "已发布");

    private final int key;
    private final String label;
}
