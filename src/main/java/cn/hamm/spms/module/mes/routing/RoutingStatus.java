package cn.hamm.spms.module.mes.routing;

import cn.hamm.airpower.dictionary.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>工艺状态</h1>
 *
 * @author Hamm.cn
 */
@AllArgsConstructor
@Getter
public enum RoutingStatus implements IDictionary {
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
