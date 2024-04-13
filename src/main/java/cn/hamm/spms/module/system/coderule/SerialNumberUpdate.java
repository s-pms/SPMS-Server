package cn.hamm.spms.module.system.coderule;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>序列号更新方式</h1>
 *
 * @author Hamm
 */
@AllArgsConstructor
@Getter
public enum SerialNumberUpdate implements IDictionary {

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

    private final int key;
    private final String label;
}
