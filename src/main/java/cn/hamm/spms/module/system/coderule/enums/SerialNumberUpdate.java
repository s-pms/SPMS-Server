package cn.hamm.spms.module.system.coderule.enums;

import cn.hamm.airpower.core.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>流水号重置周期</h1>
 *
 * @author Hamm.cn
 */
@AllArgsConstructor
@Getter
public enum SerialNumberUpdate implements IDictionary {

    /**
     * 每天重置，默认模板带完整日期
     */
    DAY(1, "按日更新", "yyyymmdd"),

    /**
     * 每月重置，默认模板带年月
     */
    MONTH(2, "按月更新", "yyyymm"),

    /**
     * 每年重置，默认模板带年份
     */
    YEAR(3, "按年更新", "yyyy"),

    /**
     * 永不重置，默认模板为空
     */
    NEVER(4, "不更新", ""),
    ;

    private final int key;
    private final String label;

    /**
     * 初始化规则时使用的默认模板
     */
    private final String defaultTemplate;
}
