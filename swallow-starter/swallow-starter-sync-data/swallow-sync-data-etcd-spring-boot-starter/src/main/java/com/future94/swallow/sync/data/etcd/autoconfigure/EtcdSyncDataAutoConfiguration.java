package com.future94.swallow.sync.data.etcd.autoconfigure;

import com.future94.swallow.data.client.etcd.EtcdSyncMetaDataService;
import com.future94.swallow.data.common.api.MetaDataSubscriber;
import io.etcd.jetcd.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author weilai
 */
@Slf4j
@Configuration
@ConditionalOnClass(EtcdSyncMetaDataService.class)
@EnableConfigurationProperties(EtcdProperties.class)
public class EtcdSyncDataAutoConfiguration {

    @Bean
    public Client etcdClient (EtcdProperties etcdProperties) {
        return Client.builder().endpoints(etcdProperties.getEndpoints().split(",")).build();
    }

    @Bean
    public EtcdSyncMetaDataService etcdSyncMetaDataService(ObjectProvider<Client> etcdClient, ObjectProvider<List<MetaDataSubscriber>> metaSubscriberList) {
        return new EtcdSyncMetaDataService(etcdClient.getIfAvailable(), metaSubscriberList.getIfAvailable());
    }
}
