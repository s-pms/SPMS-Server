package cn.hamm.starter.module.basic.supplier;

import cn.hamm.airpower.interfaces.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>供应商级别</h1>
 *
 * @author hamm
 */
@AllArgsConstructor
public enum SupplierLevel implements IEnum {

    /**
     * <h2>🏅金牌</h2>
     */
    GOLD(1, "金牌"),

    /**
     * <h2>🥈银牌</h2>
     */
    SILVER(2, "银牌"),

    /**
     * <h2>🥉铜牌</h2>
     */
    COPPER(3, "铜牌"),
    ;

    @Getter
    int value;

    @Getter
    String label;
}
