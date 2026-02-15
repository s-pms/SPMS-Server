package cn.hamm.spms;

import cn.hamm.airpower.core.exception.ServiceException;
import cn.hamm.airpower.curd.interceptor.filter.RequestFilter;
import cn.hamm.airpower.websocket.WebSocketConfig;
import cn.hamm.airpower.websocket.WebSocketHandler;
import cn.hamm.airpower.websocket.WebSocketSupport;
import cn.hamm.spms.common.AppWebSocketHandler;
import cn.hamm.spms.common.interceptor.RequestInterceptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.Objects;

/**
 * <h1>全局配置</h1>
 *
 * @author Hamm.cn
 */
@Configuration
public class WebConfig implements WebMvcConfigurer, WebSocketConfigurer {
    @Autowired
    protected WebSocketConfig webSocketConfig;
    @Autowired
    private RequestInterceptor requestInterceptor;
    @Autowired
    private AppWebSocketHandler appWebSocketHandler;

    @Override
    public void addInterceptors(@NotNull InterceptorRegistry registry) {
        registry.addInterceptor(requestInterceptor).addPathPatterns("/**");
    }

    /**
     * 获取一个 WebSocketHandler
     *
     * @return WebSocketHandler
     */
    @Bean
    public WebSocketHandler getWebSocketHandler() {
        return appWebSocketHandler;
    }

    /**
     * 注册过滤器
     *
     * @return 过滤器对象
     */
    @Bean
    public FilterRegistrationBean<RequestFilter> getFilterRegistration() {
        FilterRegistrationBean<RequestFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestFilter());
        registration.addUrlPatterns("/*");
        return registration;
    }

    /**
     * 添加 WebSocket 服务监听
     *
     * @param registry WebSocketHandlerRegistry
     */
    @Override
    public final void registerWebSocketHandlers(@NotNull WebSocketHandlerRegistry registry) {
        if (Objects.equals(webSocketConfig.getSupport(), WebSocketSupport.NO)) {
            return;
        }
        final String channelPrefix = webSocketConfig.getChannelPrefix();
        if (!StringUtils.hasText(channelPrefix)) {
            throw new ServiceException("没有配置 airpower.websocket.channelPrefix, 无法启动WebSocket服务");
        }
        registry.addHandler(getWebSocketHandler(), webSocketConfig.getPath())
                .setAllowedOrigins(webSocketConfig.getAllowedOrigins());
    }
}
