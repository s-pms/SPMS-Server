package cn.hamm.spms.module.system.coderule.enums;

import cn.hamm.airpower.web.dictionary.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>序列号更新方式</h1>
 *
 * @author Hamm.cn
 */
@AllArgsConstructor
@Getter
public enum SerialNumberUpdate implements IDictionary {

    /**
     * 按日更新
     */
    DAY(1, "按日更新", "yyyymmdd"),

    /**
     * 按月更新
     */
    MONTH(2, "按月更新", "yyyymm"),

    /**
     * 按年更新
     */
    YEAR(3, "按年更新", "yyyy"),

    /**
     * 不更新
     */
    NEVER(4, "不更新", ""),
    ;

    private final int key;
    private final String label;
    private final String defaultTemplate;
}
