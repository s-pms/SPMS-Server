package cn.hamm.spms.module.mes.bom.enums;

import cn.hamm.airpower.core.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>BOM 类型</h1>
 *
 * @author Hamm.cn
 */
@AllArgsConstructor
@Getter
public enum BomType implements IDictionary {
    /**
     * 普通配方
     */
    NORMAL(1, "普通配方"),

    /**
     * 工序配方
     */
    OPERATION(2, "工序配方"),
    ;

    private final int key;
    private final String label;
}
