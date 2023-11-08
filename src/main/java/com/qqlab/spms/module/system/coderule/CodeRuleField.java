package com.qqlab.spms.module.system.coderule;

import cn.hamm.airpower.interfaces.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>编码规则表格枚举</h1>
 *
 * @author Hamm https://hamm.cn
 */
@AllArgsConstructor
public enum CodeRuleField implements IEnum {

    /**
     * <h2>角色</h2>
     */
    RoleCode(1, "角色编码", "RO"),


    /**
     * <h2>供应商编码</h2>
     */
    SupplierCode(2, "供应商编码", "SUP"),


    /**
     * <h2>存储资源编码</h2>
     */
    StorageCode(3, "存储资源编码", "SRG"),

    ;

    @Getter
    private int value;

    @Getter
    private String label;

    @Getter
    private String defaultPrefix;
}
