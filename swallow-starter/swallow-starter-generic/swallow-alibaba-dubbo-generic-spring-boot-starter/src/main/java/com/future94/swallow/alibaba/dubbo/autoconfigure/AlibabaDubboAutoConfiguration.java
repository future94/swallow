package com.future94.swallow.alibaba.dubbo.autoconfigure;

import com.alibaba.dubbo.config.RegistryConfig;
import com.future94.swallow.data.common.api.MetaDataSubscriber;
import com.future94.swallow.web.alibaba.dubbo.cache.ReferencePathCache;
import com.future94.swallow.web.alibaba.dubbo.service.AlibabaDubboMetaDataSubscriber;
import com.future94.swallow.web.alibaba.dubbo.service.AlibabaDubboProxyService;
import com.future94.swallow.web.alibaba.dubbo.service.AlibabaDubboServiceImpl;
import com.future94.swallow.web.dubbo.common.api.DubboService;
import com.future94.swallow.web.dubbo.common.subscriber.PathMetaDataSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author weilai
 */
@Configuration
public class AlibabaDubboAutoConfiguration {

    @Bean
    @ConditionalOnClass(AlibabaDubboServiceImpl.class)
    public DubboService dubboService () {
        return new AlibabaDubboServiceImpl(new AlibabaDubboProxyService());
    }

    @Bean
    public MetaDataSubscriber alibabaDubboMetaDataSubscriber() {
        return new AlibabaDubboMetaDataSubscriber();
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
