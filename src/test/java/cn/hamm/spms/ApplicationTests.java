package cn.hamm.spms;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@SpringBootConfiguration
class ApplicationTests {
    @Test
    void initTest() {
        System.out.println("Hello AirPower!");
    }

}
