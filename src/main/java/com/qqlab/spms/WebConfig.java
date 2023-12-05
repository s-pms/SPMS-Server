package com.qqlab.spms;

import cn.hamm.airpower.security.AccessResolver;
import com.qqlab.spms.security.AccessInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * <h1>全局配置</h1h1>>
 *
 * @author Hamm
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private AccessResolver accessResolver;

    /**
     * 获取一个拦截器实例
     *
     * @return 拦截器实例
     */
    @Bean
    public AccessInterceptor getAccessInterceptor() {
        return new AccessInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //添加身份校验拦截器
        registry
                .addInterceptor(getAccessInterceptor())
                .addPathPatterns("/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(accessResolver);
    }
}