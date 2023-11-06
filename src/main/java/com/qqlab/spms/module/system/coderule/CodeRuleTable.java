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
public enum CodeRuleTable implements IEnum {

    /**
     * <h2>角色</h2>
     */
    Role(1, "角色", "role", "ROLE"),
    ;

    @Getter
    private int value;

    @Getter
    private String label;

    @Getter
    private String table;

    @Getter
    private String defaultPrefix;
}
