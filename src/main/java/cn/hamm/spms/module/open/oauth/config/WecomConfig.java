package cn.hamm.spms.module.open.oauth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * <h1>OAuth2配置文件</h1>
 *
 * @author Hamm.cn
 */
@Data
@Configuration
@ConfigurationProperties("app.oauth.wecom")
public class WecomConfig {
    /**
     * 企业 ID
     */
    private String corpId;

    /**
     * 企业密钥
     */
    private String corpSecret;

    /**
     * 应用 ID
     */
    private String agentId;
}
