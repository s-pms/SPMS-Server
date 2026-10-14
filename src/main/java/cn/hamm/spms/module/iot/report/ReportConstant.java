package cn.hamm.spms.module.iot.report;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * <h1>数据上报常量</h1>
 *
 * @author Hamm.cn
 * @apiNote {@code CACHE_PREFIX} 同时被 InfluxHelper 复用为 measurement 名，
 * 同一参数的所有设备数据落在 {@code iot:device:{code}} 这一个 measurement 里，靠 {@code uuid} tag 区分
 */
public class ReportConstant {
    /**
     * 设备采集频率下限，单位毫秒，用于 {@code DeviceEntity#rate} 的 {@code @Min} 校验
     */
    public static final int REPORT_RATE_MIN = 200;

    /**
     * 运行状态参数编码，同时会同步到 {@code DeviceEntity#status}
     */
    public static final String REPORT_KEY_OF_STATUS = "Status";

    /**
     * 实时产量参数编码，同时会同步到 {@code DeviceEntity#partCount}
     */
    public static final String REPORT_KEY_OF_PART_COUNT = "PartCnt";

    /**
     * 报警状态参数编码，同时会同步到 {@code DeviceEntity#alarm}
     */
    public static final String REPORT_KEY_OF_ALARM = "Alarm";

    /**
     * 设备数据上报订阅的 Topic
     */
    public final static String IOT_REPORT_TOPIC_V1 = "sys/msg/v1";

    /**
     * Redis 缓存 Key 与 InfluxDB measurement 名的统一前缀
     */
    public final static String CACHE_PREFIX = "iot:device:";

    /**
     * 获取设备最近一次上报内容的缓存 Key
     *
     * @param uuid 设备 UUID
     * @return key
     */
    @Contract(pure = true)
    public static @NotNull String getDeviceReportCacheKey(String uuid) {
        return CACHE_PREFIX + uuid + ":report:";
    }

    /**
     * 获取设备指定参数上一次上报值的缓存 Key
     *
     * @param code 参数编码
     * @param uuid 设备 UUID
     * @return 缓存 KEY
     * @apiNote 仅用于「值未变化则跳过写库」的短期去重，TTL 5 秒，不要当持久数据读
     */
    @Contract(pure = true)
    static @NotNull String getDeviceReportParamCacheKey(String code, String uuid) {
        return CACHE_PREFIX + uuid + ":code:" + code;
    }
}
