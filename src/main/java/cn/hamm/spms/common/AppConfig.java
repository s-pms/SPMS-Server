package cn.hamm.spms.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * <h1>应用配置文件</h1>
 *
 * @author Hamm.cn
 */
@Data
@Configuration
@ConfigurationProperties("app")
public class AppConfig {
    /**
     * 项目名称
     */
    private String projectName = "SPMS";

    /**
     * 默认房间 ID {@code 不是房间号}
     */
    private long defaultRoomId = 1L;

    /**
     * 是否是开发模式
     */
    private Boolean isDevMode = false;

    /**
     * 登录 URL
     */
    private String loginUrl;
}
