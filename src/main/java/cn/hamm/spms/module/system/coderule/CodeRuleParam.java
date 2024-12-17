package cn.hamm.spms.module.system.coderule;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>编码规则参数</h1>
 *
 * @author Hamm.cn
 */
@AllArgsConstructor
@Getter
public enum CodeRuleParam implements IDictionary {

    /**
     * <h3>完整年份</h3>
     */
    FULL_YEAR(1, "yyyy", "完整年份", "2023"),

    /**
     * <h3>年份</h3>
     */
    YEAR(2, "yy", "年份", "23"),

    /**
     * <h3>月份</h3>
     */
    MONTH(3, "mm", "月份", "12"),

    /**
     * <h3>日期</h3>
     */
    DATE(4, "dd", "日期", "31"),

    /**
     * <h3>小时</h3>
     */
    HOUR(5, "hh", "小时", "20"),
    ;

    private final int key;
    private final String label;

    /**
     * <h3>描述</h3>
     */
    private final String desc;

    /**
     * <h3>示例</h3>
     */
    private final String demo;
}
