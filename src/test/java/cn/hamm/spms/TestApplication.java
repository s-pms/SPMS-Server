package cn.hamm.spms;

import cn.hamm.spms.module.personnel.user.UserEntity;
import cn.hamm.spms.module.personnel.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class TestApplication {
    @Autowired
    private UserService userService;

    @Test
    public void run() {
        UserEntity user = userService.get(1L);
        log.info("用户: {}", user);
    }
}
