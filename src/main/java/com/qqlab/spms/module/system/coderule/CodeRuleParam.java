package com.qqlab.spms.module.system.coderule;

import cn.hamm.airpower.interfaces.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>编码规则参数</h1>
 *
 * @author Hamm
 */
@AllArgsConstructor
public enum CodeRuleParam implements IEnum {

    /**
     * <h2>完整年份</h2>
     */
    FULL_YEAR(1, "yyyy", "完整年份", "2023"),

    /**
     * <h2>年份</h2>
     */
    YEAR(2, "yy", "年份", "23"),

    /**
     * <h2>月份</h2>
     */
    MONTH(3, "mm", "月份", "12"),

    /**
     * <h2>日期</h2>
     */
    DATE(4, "dd", "日期", "31"),

    /**
     * <h2>小时</h2>
     */
    HOUR(5, "hh", "小时", "20"),
    ;

    @Getter
    private final int value;

    @Getter
    private final String label;

    @Getter
    private final String desc;

    @Getter
    private final String demo;
}
