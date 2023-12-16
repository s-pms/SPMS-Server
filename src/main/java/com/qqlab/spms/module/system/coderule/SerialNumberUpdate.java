package com.qqlab.spms.module.system.coderule;

import cn.hamm.airpower.interfaces.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>序列号更新方式</h1>
 *
 * @author Hamm
 */
@AllArgsConstructor
@Getter
public enum SerialNumberUpdate implements IEnum {

    /**
     * 按日更新
     */
    DAY(1, "按日更新"),

    /**
     * 按月更新
     */
    MONTH(2, "按月更新"),

    /**
     * 按年更新
     */
    YEAR(3, "按年更新"),

    /**
     * 不更新
     */
    NEVER(4, "不更新"),
    ;

    private final int value;
    private final String label;
}
