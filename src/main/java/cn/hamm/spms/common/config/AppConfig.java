package cn.hamm.spms.common.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Contract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * <h1>应用配置文件</h1>
 *
 * @author Hamm.cn
 */
@Component
@Data
@Accessors(chain = true)
@Configuration
@ConfigurationProperties("app")
public class AppConfig {
    /**
     * <h2>用于静态获取</h2>
     */
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private static AppConfig appConfig;

    /**
     * <h2>登录地址 用于Oauth2</h2>
     */
    private String loginUrl;
    /**
     * <h2>Influx配置</h2>
     */
    private InfluxConfig influxdb = new InfluxConfig();

    /**
     * <h2>获取应用配置</h2>
     *
     * @return 应用配置
     */
    @Contract(pure = true)
    public static AppConfig get() {
        return appConfig;
    }

    @Autowired
    private void init(
            AppConfig appConfig
    ) {
        AppConfig.appConfig = appConfig;
    }
}
