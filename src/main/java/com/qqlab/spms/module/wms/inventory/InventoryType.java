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
@Getter
public enum InventoryType implements IEnum {
    /**
     * 存储资源
     */
    STORAGE(1, "存储资源"),

    /**
     * 工厂结构
     */
    STRUCTURE(2, "工厂结构");


    private final int value;
    private final String label;
}
