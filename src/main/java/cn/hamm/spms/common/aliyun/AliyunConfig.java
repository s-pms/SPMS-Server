package cn.hamm.spms.common.aliyun;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * <h1>阿里云配置</h1>
 *
 * @author Hamm.cn
 */
@Data
@Configuration
@ConfigurationProperties("aliyun")
public class AliyunConfig {
    /**
     * AK
     */
    private String accessKeyId;

    /**
     * SK
     */
    private String accessKeySecret;
}
