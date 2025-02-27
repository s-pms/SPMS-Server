package cn.hamm.spms.common.third.aliyun.oss;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <h1>阿里云配置</h1>
 *
 * @author Hamm.cn
 */
@Component
@Data
@Accessors(chain = true)
@ConfigurationProperties("aliyun.oss")
public class AliyunOssConfig {
    /**
     * <h2>负载地址</h2>
     */
    private String endpoint;

    /**
     * <h2>bucket名称</h2>
     */
    private String bucketName;

    private String region = "cn-chengdu";
}
