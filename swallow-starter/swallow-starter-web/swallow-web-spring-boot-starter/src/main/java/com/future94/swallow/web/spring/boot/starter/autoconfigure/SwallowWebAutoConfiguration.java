package com.future94.swallow.web.spring.boot.starter.autoconfigure;

import com.future94.swallow.common.dto.DefaultSwallowResponse;
import com.future94.swallow.common.dto.SwallowCommonResponse;
import com.future94.swallow.common.dto.SwallowResponse;
import com.future94.swallow.spring.boot.common.configuration.SwallowApplicationContextAware;
import com.future94.swallow.spring.boot.web.handler.SwallowWebMvcConfigurer;
import com.future94.swallow.web.dubbo.common.api.DubboService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author weilai
 */
@Configuration
public class SwallowWebAutoConfiguration {

    @Bean
    public WebMvcConfigurer webHandler(DubboService dubboService) {
        return new SwallowWebMvcConfigurer(dubboService);
    }

    @Bean
    @ConditionalOnMissingBean(value = SwallowResponse.class, search = SearchStrategy.ALL)
    public SwallowResponse<SwallowCommonResponse> swallowResponse() {
        return new DefaultSwallowResponse();
    }

    @Bean
    public SwallowApplicationContextAware swallowApplicationContextAware() {
        return new SwallowApplicationContextAware();
    }
}
