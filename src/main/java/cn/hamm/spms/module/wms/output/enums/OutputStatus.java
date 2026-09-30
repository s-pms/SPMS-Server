package cn.hamm.spms.module.wms.output.enums;

import cn.hamm.airpower.core.interfaces.IDictionary;
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
    AUDITING(1, "审核中"),

    REJECTED(2, "已驳回"),

    OUTPUTTING(3, "出库中"),

    DONE(4, "已完成");

    private final int key;
    private final String label;
}
