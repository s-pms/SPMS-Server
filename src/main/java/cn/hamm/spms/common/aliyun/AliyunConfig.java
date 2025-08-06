package cn.hamm.spms.common.aliyun;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * <h1>阿里云配置</h1>
 *
 * @author Hamm.cn
 */
@Component
@Data
@Accessors(chain = true)
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
