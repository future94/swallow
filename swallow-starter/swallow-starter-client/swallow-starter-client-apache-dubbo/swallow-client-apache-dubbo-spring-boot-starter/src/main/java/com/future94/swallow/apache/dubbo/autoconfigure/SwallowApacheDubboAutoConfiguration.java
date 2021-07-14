package com.future94.swallow.apache.dubbo.autoconfigure;

import com.future94.swallow.client.apache.dubbo.ApacheDubboServiceBeanPostProcessor;
import com.future94.swallow.common.dto.ServerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author weilai
 */
@Configuration
public class SwallowApacheDubboAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "swallow.server")
    public ServerConfig serverConfig() {
        return new ServerConfig();
    }

    @Bean
    public ApacheDubboServiceBeanPostProcessor apacheDubboServiceBeanPostProcessor(ServerConfig serverConfig) {
        return new ApacheDubboServiceBeanPostProcessor(serverConfig);
    }
}
