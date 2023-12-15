package com.qqlab.spms.module.iot.report;

import cn.hamm.airpower.interfaces.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据类型
 *
 * @author hamm
 */

@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
@AllArgsConstructor
@Getter
public enum ReportDataType implements IEnum {
    QUANTITY(1, "数量"),
    STATUS(2, "状态"),
    SWITCH(3, "开关"),
    INFORMATION(4, "信息"),
    ;
    private final int value;

    private final String label;
}
