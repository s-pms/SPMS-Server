package cn.hamm.spms.common.config;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * <h1>应用配置文件</h1>
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
     * <h2>登录地址 用于Oauth2</h2>
     */
    private String loginUrl;

    /**
     * <h2>Influx配置</h2>
     */
    private InfluxConfig influxdb = new InfluxConfig();

    /**
     * <h2>是否初始化数据</h2>
     */
    private boolean initData = false;
}
