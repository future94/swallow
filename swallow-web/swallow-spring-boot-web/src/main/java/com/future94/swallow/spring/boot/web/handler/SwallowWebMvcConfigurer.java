package com.future94.swallow.spring.boot.web.handler;

import com.future94.swallow.web.dubbo.common.api.DubboService;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author weilai
 */
public class SwallowWebMvcConfigurer implements WebMvcConfigurer {

    private final DubboService dubboService;

    public SwallowWebMvcConfigurer(DubboService dubboService) {
        this.dubboService = dubboService;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 可添加多个
        registry.addInterceptor(new SwallowWebHandler(dubboService)).addPathPatterns("/**");
    }
}
