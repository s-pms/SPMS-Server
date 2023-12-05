package com.qqlab.spms.module.asset.material;

import cn.hamm.airpower.interfaces.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>物料类型枚举</h1>
 *
 * @author Hamm
 */
@AllArgsConstructor
public enum MaterialType implements IEnum {
    /**
     * 自产品
     */
    PRODUCT(1, "自产品"),

    /**
     * 外购品
     */
    PURCHASE(2, "外购品");

    @Getter
    private final int value;

    @Getter
    private final String label;
}
