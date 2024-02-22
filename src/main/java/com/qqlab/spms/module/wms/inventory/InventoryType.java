package com.qqlab.spms.module.wms.inventory;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>库存类型</h1>
 *
 * @author Hamm
 */
@AllArgsConstructor
@Getter
public enum InventoryType implements IDictionary {
    /**
     * 存储资源
     */
    STORAGE(1, "存储资源"),

    /**
     * 工厂结构
     */
    STRUCTURE(2, "工厂结构");


    private final int key;
    private final String label;
}
