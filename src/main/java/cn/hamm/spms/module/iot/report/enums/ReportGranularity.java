package cn.hamm.spms.module.iot.report.enums;

import cn.hamm.airpower.core.interfaces.IDictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <h1>报告颗粒度</h1>
 *
 * @author Hamm.cn
 * @apiNote {@code key} 是秒数（{@code 2678400} 是按 31 天算的），{@code mark} 是 Flux
 * {@code aggregateWindow(every: ...)} 要的时长字面量，只有 {@code ReportDataType.NUMBER} 会用到它
 */
@AllArgsConstructor
@Getter
public enum ReportGranularity implements IDictionary {
    /**
     * 每分钟
     */
    ONE_MINUTE(60, "每分钟", "1m"),

    /**
     * 每五分钟
     */
    FIVE_MINUTES(300, "每五分钟", "5m"),

    /**
     * 每半小时
     */
    THIRTY_MINUTES(1800, "每半小时", "30m"),

    /**
     * 每小时
     */
    ONE_HOUR(3600, "每小时", "1h"),

    /**
     * 每天
     */
    ONE_DAY(86400, "每天", "24h"),

    /**
     * 每周
     */
    ONE_WEEK(604800, "每周", "168h"),

    /**
     * 每月：按 31 天折算
     */
    ONE_MONTH(2678400, "每月", "744h"),
    ;

    private final int key;
    private final String label;
    private final String mark;
}
