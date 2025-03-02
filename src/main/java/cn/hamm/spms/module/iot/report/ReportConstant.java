package cn.hamm.spms.module.iot.report;

import cn.hamm.airpower.config.Constant;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * <h1>数据上报常量</h1>
 *
 * @author Hamm.cn
 */
public class ReportConstant {
    /**
     * <h3>数据上报毫秒最小</h3>
     */
    public static final int REPORT_RATE_MIN = 200;

    /**
     * <h3>运行状态</h3>
     */
    public static final String REPORT_KEY_OF_STATUS = "Status";

    /**
     * <h3>产量事件</h3>
     */
    public static final String REPORT_KEY_OF_PART_COUNT = "PartCnt";

    /**
     * <h3>报警事件</h3>
     */
    public static final String REPORT_KEY_OF_ALARM = "Alarm";

    /**
     * <h3>订阅Topic</h3>
     */
    public final static String IOT_REPORT_TOPIC_V1 = "sys/msg/v1";

    /**
     * <h3>Redis存IOT采集数据的前缀</h3>
     */
    public final static String CACHE_PREFIX = "iot_report_";

    /**
     * <h3>获取设备报告缓存的key</h3>
     *
     * @param uuid UUID
     * @return key
     */
    @Contract(pure = true)
    public static @NotNull String getDeviceReportCacheKey(String uuid) {
        return CACHE_PREFIX + uuid;
    }

    /**
     * <h2>获取设备指定参数的缓存Key</h2>
     *
     * @param code 参数编码
     * @param uuid 设备UUID
     * @return 缓存KEY
     */
    @Contract(pure = true)
    static @NotNull String getDeviceReportParamCacheKey(String code, String uuid) {
        return CACHE_PREFIX + code + Constant.STRING_UNDERLINE + uuid;
    }
}
