package cn.hamm.spms.module.asset.material;

import cn.hamm.airpower.interfaces.IDictionary;
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
     * <h3>工具类</h3>
     */
    TOOL(1, "工具类"),

    /**
     * <h3>消耗品</h3>
     */
    CONSUMABLE(2, "消耗品");

    private final int key;
    private final String label;
}
