package cn.hamm.spms.module.mes.picking;

import cn.hamm.airpower.dictionary.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>领料单类型</h1>
 *
 * @author Hamm.cn
 */
@AllArgsConstructor
@Getter
public enum PickingType implements IDictionary {
    /**
     * 生产领料
     */
    PRODUCE(1, "生产领料"),

    /**
     * 其他领料
     */
    OTHER(2, "其他领料");

    private final int key;
    private final String label;
}
