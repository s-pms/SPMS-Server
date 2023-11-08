package com.qqlab.spms.module.system.coderule;

import cn.hamm.airpower.interfaces.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>序列号更新方式</h1>
 *
 * @author Hamm https://hamm.cn
 */
@AllArgsConstructor
public enum SerialNumberUpdate implements IEnum {

    /**
     * <h2>按日更新</h2>
     */
    DAY(1, "按日更新"),

    /**
     * <h2>按月更新</h2>
     */
    MONTH(2, "按月更新"),

    /**
     * <h2>按年更新</h2>
     */
    YEAR(3, "按年更新"),

    /**
     * <h2>不更新</h2>
     */
    NEVER(4, "不更新"),
    ;

    @Getter
    private int value;

    @Getter
    private String label;
}
