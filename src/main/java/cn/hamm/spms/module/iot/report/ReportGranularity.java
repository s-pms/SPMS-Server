package cn.hamm.spms.module.iot.report;

import cn.hamm.airpower.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>报告颗粒度</h1>
 *
 * @author Hamm.cn
 */
@AllArgsConstructor
@Getter
public enum ReportGranularity implements IDictionary {
    /**
     * <h3>每分钟</h3>
     */
    ONE_MINUTE(60, "每分钟", "1m"),

    /**
     * <h3>每五分钟</h3>
     */
    FIVE_MINUTES(300, "每五分钟", "5m"),

    /**
     * <h3>每半小时</h3>
     */
    THIRTY_MINUTES(1800, "每半小时", "30m"),

    /**
     * <h3>每小时</h3>
     */
    ONE_HOUR(3600, "每小时", "1h"),

    /**
     * <h3>每天</h3>
     */
    ONE_DAY(86400, "每天", "24h"),

    /**
     * <h3>每周</h3>
     */
    ONE_WEEK(604800, "每周", "168h"),

    /**
     * <h3>每月</h3>
     */
    ONE_MONTH(2678400, "每月", "744h"),
    ;

    private final int key;
    private final String label;
    private final String mark;
}
