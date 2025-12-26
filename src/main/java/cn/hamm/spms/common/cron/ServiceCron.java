package cn.hamm.spms.common.cron;

import cn.hamm.airpower.api.ApiConfig;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * <h1>系统定时任务</h1>
 *
 * @author Hamm.cn
 */
@Component
public class ServiceCron {
    @Scheduled(cron = "0 0 0 * * *")
    void test() {
        ApiConfig.isServerRunning = !ApiConfig.isServerRunning;
    }
}
