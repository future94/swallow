package com.future94.swallow.sync.data.zookeeper.autoconfigure;

import com.future94.swallow.data.client.zookeeper.ZookeeperSyncMetaDataService;
import com.future94.swallow.data.common.api.MetaDataSubscriber;
import com.future94.swallow.data.common.api.SyncMetaDataService;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

/**
 * @author weilai
 */
@Slf4j
@Configuration
@ConditionalOnClass(ZookeeperSyncMetaDataService.class)
@EnableConfigurationProperties(ZookeeperProperties.class)
@ConditionalOnProperty(prefix = "swallow.sync.zookeeper", name = "zk-servers")
public class ZookeeperSyncDataAutoConfiguration {

    @Bean
    public ZkClient zkClient(ZookeeperProperties zookeeperProperties) {
        return new ZkClient(zookeeperProperties.getZkServers(), zookeeperProperties.getSessionTimeout(), zookeeperProperties.getConnectionTimeout(), zookeeperProperties.getZkSerializer(), zookeeperProperties.getOperationRetryTimeout());
    }

    @Bean
    public SyncMetaDataService zookeeperSyncMetaDataService(ObjectProvider<ZkClient> zkClient, ObjectProvider<List<MetaDataSubscriber>> metaDataSubscriberList) {
        return new ZookeeperSyncMetaDataService(zkClient.getIfAvailable(), metaDataSubscriberList.getIfAvailable(Collections::emptyList));
    }
}
