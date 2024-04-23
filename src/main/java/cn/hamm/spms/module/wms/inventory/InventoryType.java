package cn.hamm.spms.module.wms.inventory;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>库存类型</h1>
 *
 * @author Hamm.cn
 */
@AllArgsConstructor
@Getter
public enum InventoryType implements IDictionary {
    /**
     * <h2>存储资源</h2>
     */
    STORAGE(1, "存储资源"),

    /**
     * <h2>工厂结构</h2>
     */
    STRUCTURE(2, "工厂结构");


    private final int key;
    private final String label;
}
