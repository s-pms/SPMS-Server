package cn.hamm.spms.module.factory.structure;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>生产单元类型</h1>
 *
 * @author Hamm.cn
 */
@Getter
@AllArgsConstructor
public enum StructureType implements IDictionary {
    /**
     * <h3>单人工位</h3>
     */
    SINGLE_STATION(1, "单人工位"),

    /**
     * <h3>公共工区</h3>
     */
    COMMON_WORK_AREA(2, "公共工区"),

    /**
     * <h3>多人工区</h3>
     */
    MULTI_STATION(3, "多人工区"),

    /**
     * <h3>轮用工区</h3>
     */
    ROUND_WORK_AREA(4, "轮用工区");


    private final int key;
    private final String label;
}
