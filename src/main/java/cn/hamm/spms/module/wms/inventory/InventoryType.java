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
     * <h3>仓库</h3>
     */
    STORAGE(1, "仓库"),

    /**
     * <h3>生产单元</h3>
     */
    STRUCTURE(2, "生产单元");


    private final int key;
    private final String label;
}
