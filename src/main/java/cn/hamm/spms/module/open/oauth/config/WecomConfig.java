package cn.hamm.spms.module.open.oauth.config;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * <h1>OAuth2配置文件</h1>
 *
 * @author Hamm.cn
 */
@Component
@Data
@Accessors(chain = true)
@Configuration
@ConfigurationProperties("app.oauth.wecom")
public class WecomConfig {
    private String corpId;

    private String corpSecret;

    private String agentId;
}
