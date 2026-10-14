package cn.hamm.spms.module.wms.move.enums;

import cn.hamm.airpower.core.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>移库状态</h1>
 *
 * @author Hamm.cn
 */
@AllArgsConstructor
@Getter
public enum MoveStatus implements IDictionary {
    AUDITING(1, "审核中"),

    REJECTED(2, "已驳回"),

    MOVING(3, "移动中"),

    DONE(4, "已完成");

    private final int key;
    private final String label;
}
