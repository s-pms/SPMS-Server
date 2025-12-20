package cn.hamm.spms;

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
    }
}
