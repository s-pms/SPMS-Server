package com.qqlab.spms.module.wms.inventory;

import cn.hamm.airpower.interfaces.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>库存类型</h1>
 *
 * @author Hamm
 */
@AllArgsConstructor
public enum InventoryType implements IEnum {
    /**
     * <h2>存储资源</h2>
     */
    STORAGE(1, "存储资源"),

    /**
     * <h2>工厂结构</h2>
     */
    STRUCTURE(2, "工厂结构");


    @Getter
    private int value;

    @Getter
    private String label;
}
