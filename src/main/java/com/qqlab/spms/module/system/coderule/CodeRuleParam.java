package com.qqlab.spms.module.system.coderule;

import cn.hamm.airpower.interfaces.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>编码规则参数</h1>
 *
 * @author Hamm https://hamm.cn
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
    private int value;

    @Getter
    private String label;

    @Getter
    private String desc;

    @Getter
    private String demo;
}
