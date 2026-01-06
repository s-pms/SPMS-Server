package cn.hamm.spms;

import cn.hamm.airpower.web.AbstractWebConfig;
import cn.hamm.airpower.web.websocket.WebSocketHandler;
import cn.hamm.spms.common.AppWebSocketHandler;
import cn.hamm.spms.common.interceptor.RequestInterceptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

/**
 * <h1>全局配置</h1>
 *
 * @author Hamm.cn
 */
@Configuration
public class WebConfig extends AbstractWebConfig {
    @Autowired
    private RequestInterceptor requestInterceptor;

    @Autowired
    private AppWebSocketHandler appWebSocketHandler;

    @Override
    public WebSocketHandler getWebSocketHandler() {
        return appWebSocketHandler;
    }

    @Override
    public void addInterceptors(@NotNull InterceptorRegistry registry) {
        registry.addInterceptor(requestInterceptor).addPathPatterns("/**");
    }
}
