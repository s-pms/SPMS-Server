package cn.hamm.spms.module.asset.material.enums;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>物料类型枚举</h1>
 *
 * @author Hamm.cn
 */
@AllArgsConstructor
@Getter
public enum MaterialType implements IDictionary {
    /**
     * <h3>自产品</h3>
     */
    PRODUCT(1, "自产品"),

    /**
     * <h3>外购品</h3>
     */
    PURCHASE(2, "外购品");

    private final int key;
    private final String label;
}
