package cn.hamm.spms.module.mes.bom.enums;

import cn.hamm.airpower.core.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>BOM 状态</h1>
 *
 * @author Hamm.cn
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
