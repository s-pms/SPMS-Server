package cn.hamm.spms.common.third.aliyun.sms;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <h1>阿里云短信配置</h1>
 *
 * @author zhoul
 **/
@Data
@Component
@ConfigurationProperties("aliyun.sms")
public class AliyunSmsConfig {
    /**
     * <h2>签名</h2>
     */
    private String signName;

    /**
     * <h2>登录模板</h2>
     */
    private String loginTemplate = "SMS_464330180";
}
