package cn.hamm.spms.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * <h1>InfluxDB 配置</h1>
 *
 * @author Hamm.cn
 */
@Data
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
    private String token = "spms";

    /**
     * 使用的组织名
     */
    private String org = "spms";

    /**
     * 使用的数据库
     */
    private String bucket = "spms";
}
