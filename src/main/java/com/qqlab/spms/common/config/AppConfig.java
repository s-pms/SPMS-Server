package com.qqlab.spms.common.config;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * 应用配置文件
 *
 * @author Hamm
 */
@Component
@Data
@Accessors(chain = true)
@Configuration
@ConfigurationProperties("app")
public class AppConfig {
    /**
     * 登录地址 用于Oauth2
     */
    private String loginUrl;

    /**
     * Influx配置
     */
    private InfluxConfig influxdb = new InfluxConfig();
}
