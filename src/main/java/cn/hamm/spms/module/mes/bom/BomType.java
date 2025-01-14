package cn.hamm.spms.module.mes.bom;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>BOM类型</h1>
 *
 * @author Hamm.cn
 */
@AllArgsConstructor
@Getter
public enum BomType implements IDictionary {
    /**
     * <h3>普通配方</h3>
     */
    NORMAL(1, "普通配方"),

    /**
     * <h3>工序配方</h3>
     */
    OPERATION(2, "工序配方"),
    ;

    private final int key;
    private final String label;
}
