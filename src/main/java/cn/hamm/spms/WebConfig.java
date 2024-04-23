package cn.hamm.spms;

import cn.hamm.airpower.AbstractWebConfig;
import cn.hamm.airpower.interceptor.AbstractRequestInterceptor;
import cn.hamm.spms.common.interceptor.RequestInterceptor;
import org.springframework.context.annotation.Configuration;

/**
 * <h1>全局配置</h1h1>>
 *
 * @author Hamm.cn
 */
@Configuration
public class WebConfig extends AbstractWebConfig {

    @Override
    public AbstractRequestInterceptor getAccessInterceptor() {
        return new RequestInterceptor();
    }
}