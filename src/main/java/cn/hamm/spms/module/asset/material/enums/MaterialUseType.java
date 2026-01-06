package cn.hamm.spms.module.asset.material.enums;

import cn.hamm.airpower.core.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>物料使用类型枚举</h1>
 *
 * @author Hamm.cn
 */
@AllArgsConstructor
@Getter
public enum MaterialUseType implements IDictionary {
    /**
     * 工具类
     */
    TOOL(1, "工具类"),

    /**
     * 消耗品
     */
    CONSUMABLE(2, "消耗品");

    private final int key;
    private final String label;
}
