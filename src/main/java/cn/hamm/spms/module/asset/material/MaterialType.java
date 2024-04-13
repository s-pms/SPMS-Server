package cn.hamm.spms.module.asset.material;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>物料类型枚举</h1>
 *
 * @author Hamm
 */
@AllArgsConstructor
@Getter
public enum MaterialType implements IDictionary {
    /**
     * <h2>自产品</h2>
     */
    PRODUCT(1, "自产品"),

    /**
     * <h2>外购品</h2>
     */
    PURCHASE(2, "外购品");

    private final int key;
    private final String label;
}
