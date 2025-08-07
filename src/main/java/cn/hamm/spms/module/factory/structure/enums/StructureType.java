package cn.hamm.spms.module.factory.structure.enums;

import cn.hamm.airpower.dictionary.IDictionary;
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
     * 单人工位
     */
    SINGLE_STATION(1, "单人工位"),

    /**
     * 公共工区
     */
    COMMON_WORK_AREA(2, "公共工区"),

    /**
     * 多人工区
     */
    MULTI_STATION(3, "多人工区"),

    /**
     * 轮用工区
     */
    ROUND_WORK_AREA(4, "轮用工区");

    private final int key;
    private final String label;
}
