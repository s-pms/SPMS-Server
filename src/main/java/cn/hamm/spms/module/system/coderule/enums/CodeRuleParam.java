package cn.hamm.spms.module.system.coderule.enums;

import cn.hamm.airpower.core.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>编码模板参数</h1>
 *
 * @author Hamm.cn
 * @apiNote {@code label} 是写进模板的占位符，{@code desc} 是给前端看的说明，{@code demo} 是渲染示例
 */
@AllArgsConstructor
@Getter
public enum CodeRuleParam implements IDictionary {

    /**
     * 完整年份，如 2023
     */
    FULL_YEAR(1, "yyyy", "完整年份", "2023"),

    /**
     * 两位年份，如 23
     */
    YEAR(2, "yy", "年份", "23"),

    /**
     * 两位月份，如 09
     */
    MONTH(3, "mm", "月份", "12"),

    /**
     * 两位日期，如 28
     */
    DATE(4, "dd", "日期", "31"),

    /**
     * 两位小时，如 08
     */
    HOUR(5, "hh", "小时", "20"),
    ;

    private final int key;

    /**
     * 模板中的占位符
     */
    private final String label;

    /**
     * 占位符含义
     */
    private final String desc;

    /**
     * 渲染示例
     */
    private final String demo;
}
