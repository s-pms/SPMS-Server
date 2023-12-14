package com.qqlab.spms.module.iot.report;

import cn.hamm.airpower.interfaces.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 报告颗粒度
 *
 * @author hamm
 */
@AllArgsConstructor
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
public enum ReportGranularity implements IEnum {
    FIVE_SECONDS(5, "每五秒"),

    THIRTY_SECONDS(30, "每半分钟"),

    ONE_MINUTE(60, "每分钟"),

    FIVE_MINUTES(300, "每五分钟"),

    THIRTY_MINUTES(1800, "每半小时"),

    ONE_HOUR(3600, "每小时"),

    ONE_MONTH(86400, "每天"),
    ;

    @Getter
    private final int value;
    @Getter
    private final String label;
}
