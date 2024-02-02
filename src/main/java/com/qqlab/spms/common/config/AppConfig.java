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
     * 应用版本Header
     */
    private String appVersionHeader = "app-version";

    /**
     * 应用版本平台
     */
    private String appPlatformHeader = "app-platform";

    /**
     * 登录地址 用于Oauth2
     */
    private String loginUrl;

    /**
     * Influx配置
     */
    private InfluxConfig influxdb = new InfluxConfig();

    /**
     * 是否初始化数据
     */
    private boolean initData = false;
}
