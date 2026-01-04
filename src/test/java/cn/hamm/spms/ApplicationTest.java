package cn.hamm.spms;

import cn.hamm.airpower.core.TaskUtil;
import cn.hamm.airpower.web.redis.RedisHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest
@ActiveProfiles("local-hamm")
public class ApplicationTest {
    @Autowired
    private RedisHelper redisHelper;

    @Test
    public void lockTest() {
        log.info("test");
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            TaskUtil.runAsync(() -> test(finalI));
        }
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void test(int i) {
        log.info("{}", i);
        redisHelper.runWithLock("xxx_" + (i % 3), () -> {
            log.info("Locked {}", i);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
