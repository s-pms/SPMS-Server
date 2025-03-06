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
     * <h3>负载地址</h3>
     */
    private String endpoint;

    /**
     * <h3>bucket名称</h3>
     */
    private String bucketName;

    /**
     * <h3>区域</h3>
     */
    private String region = "cn-chengdu";
}
