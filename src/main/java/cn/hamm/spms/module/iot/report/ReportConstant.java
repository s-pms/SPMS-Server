package cn.hamm.spms.module.iot.report;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * <h1>数据上报常量</h1>
 *
 * @author Hamm.cn
 */
public class ReportConstant {
    /**
     * 数据上报毫秒最小
     */
    public static final int REPORT_RATE_MIN = 200;

    /**
     * 运行状态
     */
    public static final String REPORT_KEY_OF_STATUS = "Status";

    /**
     * 产量事件
     */
    public static final String REPORT_KEY_OF_PART_COUNT = "PartCnt";

    /**
     * 报警事件
     */
    public static final String REPORT_KEY_OF_ALARM = "Alarm";

    /**
     * 订阅Topic
     */
    public final static String IOT_REPORT_TOPIC_V1 = "sys/msg/v1";

    /**
     * Redis存IOT采集数据的前缀
     */
    public final static String CACHE_PREFIX = "iot_report_";

    /**
     * 获取设备报告缓存的key
     *
     * @param uuid UUID
     * @return key
     */
    @Contract(pure = true)
    public static @NotNull String getDeviceReportCacheKey(String uuid) {
        return CACHE_PREFIX + uuid;
    }

    /**
     * 获取设备指定参数的缓存Key
     *
     * @param code 参数编码
     * @param uuid 设备UUID
     * @return 缓存KEY
     */
    @Contract(pure = true)
    static @NotNull String getDeviceReportParamCacheKey(String code, String uuid) {
        return CACHE_PREFIX + code + "_" + uuid;
    }
}
