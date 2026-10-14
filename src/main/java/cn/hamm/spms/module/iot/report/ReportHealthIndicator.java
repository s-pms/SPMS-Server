package cn.hamm.spms.module.iot.report;

import cn.hamm.spms.common.Configs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * <h1>设备采集健康检查</h1>
 *
 * @author Hamm.cn
 * @apiNote 暴露为 {@code /actuator/health} 的 {@code report} 分项。
 * 采集链路是「沉默即故障」的系统——订阅断开时不会抛异常也不会有日志，
 * 所以必须靠「多久没收到数据」把这种静默失效变成可被监控发现的显式信号
 */
@Component("report")
@Slf4j
public class ReportHealthIndicator implements HealthIndicator {
    private final ReportMqCallback reportMqCallback;

    public ReportHealthIndicator(ReportMqCallback reportMqCallback) {
        this.reportMqCallback = reportMqCallback;
    }

    /**
     * 检查设备采集链路是否仍在收到数据
     *
     * @return 静默时长在阈值内为 UP，超过阈值或已发生连接断开为 DOWN
     * @apiNote 阈值取 {@code app.iot-report-silence-seconds}，为 0 时关闭本项检查并恒返回 UP
     */
    @Override
    public Health health() {
        long silenceSeconds = Configs.getAppConfig().getIotReportSilenceSeconds();
        if (silenceSeconds <= 0) {
            return Health.up().withDetail("enabled", false).build();
        }
        long silenceMillis = TimeUnit.SECONDS.toMillis(silenceSeconds);
        long silentMillis = System.currentTimeMillis() - reportMqCallback.getLastReportAt();
        int lostCount = reportMqCallback.getConnectionLostCount();
        // 静默时长与断连次数都写进 details，便于 show-details 打开后直接定位是哪种故障
        Health.Builder builder = silentMillis > silenceMillis ? Health.down() : Health.up();
        return builder
                .withDetail("silentSeconds", silentMillis / 1000)
                .withDetail("thresholdSeconds", silenceSeconds)
                .withDetail("connectionLostCount", lostCount)
                .withDetail("lastReportAt", Instant.ofEpochMilli(reportMqCallback.getLastReportAt()))
                .build();
    }
}
