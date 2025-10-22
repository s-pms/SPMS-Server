package cn.hamm.spms.common.aliyun.oss;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * <h1>阿里云配置</h1>
 *
 * @author Hamm.cn
 */
@Data
@Accessors(chain = true)
@Configuration
@ConfigurationProperties("aliyun.oss")
public class AliyunOssConfig {
    /**
     * 负载地址
     */
    private String endpoint;

    /**
     * bucket名称
     */
    private String bucketName;

    /**
     * 区域
     */
    private String region = "cn-chengdu";
}
