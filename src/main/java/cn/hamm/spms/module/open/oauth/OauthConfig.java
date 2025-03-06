package cn.hamm.spms.module.open.oauth;

import cn.hamm.spms.module.open.oauth.config.WecomConfig;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <h1>{@code OAuth2} 配置</h1>
 *
 * @author Hamm.cn
 */
@Component
public class OauthConfig {
    @Getter
    private static WecomConfig wecomConfig;

    @Autowired
    OauthConfig(WecomConfig wecomConfig) {
        OauthConfig.wecomConfig = wecomConfig;
    }
}
