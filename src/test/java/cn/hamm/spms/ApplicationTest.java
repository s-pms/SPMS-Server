package cn.hamm.spms;

import cn.hamm.airpower.util.TaskUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest
@ActiveProfiles("local-hamm")
public class ApplicationTest {
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
        TaskUtil.runWithLock("xxx_" + (i % 3), () -> {
            log.info("Locked {}", i);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
