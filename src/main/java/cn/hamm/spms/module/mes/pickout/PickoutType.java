package cn.hamm.spms.module.mes.pickout;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>领料单类型</h1>
 *
 * @author Hamm.cn
 */
@AllArgsConstructor
@Getter
public enum PickoutType implements IDictionary {
    /**
     * <h2>生产领料</h2>
     */
    PRODUCE(1, "生产领料"),

    /**
     * <h2>其他领料</h2>
     */
    OTHER(2, "其他领料");

    private final int key;
    private final String label;
}
