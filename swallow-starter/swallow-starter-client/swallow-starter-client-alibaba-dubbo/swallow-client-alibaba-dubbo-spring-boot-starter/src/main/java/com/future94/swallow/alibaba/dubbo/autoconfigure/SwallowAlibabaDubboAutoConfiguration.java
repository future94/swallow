package com.future94.swallow.alibaba.dubbo.autoconfigure;

import com.future94.swallow.client.alibaba.dubbo.AlibabaDubboServiceBeanPostProcessor;
import com.future94.swallow.common.dto.ServerConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author weilai
 */
@Configuration
public class SwallowAlibabaDubboAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "swallow.server")
    public ServerConfig serverConfig() {
        return new ServerConfig();
    }

    @Bean
    public AlibabaDubboServiceBeanPostProcessor alibabaDubboServiceBeanPostProcessor(ServerConfig serverConfig) {
        return new AlibabaDubboServiceBeanPostProcessor(serverConfig);
    }
}
