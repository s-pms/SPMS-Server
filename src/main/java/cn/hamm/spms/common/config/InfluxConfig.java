package cn.hamm.spms.common.config;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * <h1>InfluxDB配置</h1>
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
     * <h2>连接地址</h2>
     */
    private String url = "http://127.0.0.1:8086";

    /**
     * <h2>连接令牌</h2>
     */
    private String token = "spms";

    /**
     * <h2>使用的组织名</h2>
     */
    private String org = "spms";

    /**
     * <h2>使用的数据库</h2>
     */
    private String bucket = "spms";
}
