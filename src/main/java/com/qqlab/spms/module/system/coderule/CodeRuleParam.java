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
     * 完整年份
     */
    FULL_YEAR(1, "yyyy", "完整年份", "2023"),

    /**
     * 年份
     */
    YEAR(2, "yy", "年份", "23"),

    /**
     * 月份
     */
    MONTH(3, "mm", "月份", "12"),

    /**
     * 日期
     */
    DATE(4, "dd", "日期", "31"),

    /**
     * 小时
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
