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
@Getter
public enum ReportGranularity implements IEnum {
    FIVE_SECONDS(5, "每五秒", "5s"),

    THIRTY_SECONDS(30, "每半分钟", "30s"),

    ONE_MINUTE(60, "每分钟", "1m"),

    FIVE_MINUTES(300, "每五分钟", "5m"),

    THIRTY_MINUTES(1800, "每半小时", "30m"),

    ONE_HOUR(3600, "每小时", "1h"),

    ONE_MONTH(86400, "每天", "24h"),
    ;

    private final int value;
    private final String label;
    private final String mark;
}
