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
     * 仓库
     */
    STORAGE(1, "仓库"),

    /**
     * 生产单元
     */
    STRUCTURE(2, "生产单元");

    private final int key;
    private final String label;
}
