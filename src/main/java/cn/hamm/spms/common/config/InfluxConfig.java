package cn.hamm.spms.common.config;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * InfluxDB配置
 *
 * @author Hamm
 */
@Component
@Data
@Accessors(chain = true)
@Configuration
@ConfigurationProperties("app.influxdb")
public class InfluxConfig {
    /**
     * 连接地址
     */
    private String url = "http://127.0.0.1:8086";

    /**
     * 连接令牌
     */
    private String token = "airpower";

    /**
     * 使用的组织名
     */
    private String org = "airpower";

    /**
     * 使用的数据库
     */
    private String bucket = "airpower";
}
