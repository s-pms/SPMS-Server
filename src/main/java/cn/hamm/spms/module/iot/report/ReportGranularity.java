package cn.hamm.spms.module.iot.report;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 报告颗粒度
 *
 * @author Hamm
 */
@AllArgsConstructor
@SuppressWarnings("AlibabaEnumConstantsMustHaveComment")
@Getter
public enum ReportGranularity implements IDictionary {
    ONE_MINUTE(60, "每分钟", "1m"),

    FIVE_MINUTES(300, "每五分钟", "5m"),

    THIRTY_MINUTES(1800, "每半小时", "30m"),

    ONE_HOUR(3600, "每小时", "1h"),

    ONE_DAY(86400, "每天", "24h"),

    ONE_WEEK(604800, "每周", "168h"),

    ONE_MONTH(2678400, "每月", "744h"),
    ;

    private final int key;
    private final String label;
    private final String mark;
}
