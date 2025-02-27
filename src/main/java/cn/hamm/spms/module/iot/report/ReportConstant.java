package cn.hamm.spms.module.iot.report;

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
}
