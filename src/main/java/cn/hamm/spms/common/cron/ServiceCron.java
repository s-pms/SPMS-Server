package cn.hamm.spms.common.cron;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * <h1>系统定时任务</h1>
 *
 * @author Hamm.cn
 */
@Slf4j
@Component
public class ServiceCron {
    @Scheduled(cron = "1/5 * * * * *")
    void test() {
        log.info("测试定时任务");
    }
}
