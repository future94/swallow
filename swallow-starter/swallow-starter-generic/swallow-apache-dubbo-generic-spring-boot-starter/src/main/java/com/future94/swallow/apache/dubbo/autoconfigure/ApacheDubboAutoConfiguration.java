package com.future94.swallow.apache.dubbo.autoconfigure;

import com.future94.swallow.data.common.api.MetaDataSubscriber;
import com.future94.swallow.web.apache.dubbo.cache.ReferencePathCache;
import com.future94.swallow.web.apache.dubbo.service.ApacheDubboMetaDataSubscriber;
import com.future94.swallow.web.apache.dubbo.service.ApacheDubboProxyService;
import com.future94.swallow.web.apache.dubbo.service.ApacheDubboServiceImpl;
import com.future94.swallow.web.dubbo.common.api.DubboService;
import com.future94.swallow.web.dubbo.common.subscriber.PathMetaDataSubscriber;
import org.apache.dubbo.config.RegistryConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author weilai
 */
@Configuration
public class ApacheDubboAutoConfiguration {

    @Bean
    @ConditionalOnClass(ApacheDubboServiceImpl.class)
    public DubboService dubboService () {
        return new ApacheDubboServiceImpl(new ApacheDubboProxyService());
    }

    @Bean
    public MetaDataSubscriber apacheDubboMetaDataSubscriber() {
        return new ApacheDubboMetaDataSubscriber();
    }

    @Bean
    @ConfigurationProperties(prefix = "dubbo.registry")
    public RegistryConfig dubboRegisterConfig() {
        return new RegistryConfig();
    }

    @Bean
    public MetaDataSubscriber metaDataAllSubscriber() {
        return new PathMetaDataSubscriber();
    }

    @Autowired
    public void setRegistryConfig(RegistryConfig registryConfig) {
        ReferencePathCache.init(registryConfig);
    }
}
