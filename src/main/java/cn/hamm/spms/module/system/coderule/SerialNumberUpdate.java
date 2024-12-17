package cn.hamm.spms.module.system.coderule;

import cn.hamm.airpower.interfaces.IDictionary;
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
     * <h3>按日更新</h3>
     */
    DAY(1, "按日更新"),

    /**
     * <h3>按月更新</h3>
     */
    MONTH(2, "按月更新"),

    /**
     * <h3>按年更新</h3>
     */
    YEAR(3, "按年更新"),

    /**
     * <h3>不更新</h3>
     */
    NEVER(4, "不更新"),
    ;

    private final int key;
    private final String label;
}
