package com.future94.swallow.data.client.bootstrap.config;

import com.future94.swallow.data.client.bootstrap.listener.DataChangeEventMulticaster;
import com.future94.swallow.data.client.bootstrap.listener.DataChangedListener;
import com.future94.swallow.data.client.bootstrap.listener.http.HttpLongPollingDataChangedListener;
import com.future94.swallow.data.client.bootstrap.listener.zookeeper.ZookeeperDataChangedListener;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author weilai
 */
@Configuration
public class DataSyncConfiguration {

    /**
     * http long polling.
     */
    @Configuration
    @ConditionalOnProperty(name = "swallow.sync.http.enabled", havingValue = "true")
    @EnableConfigurationProperties(HttpSyncProperties.class)
    static class HttpLongPollingListener {

        @Bean
        @ConditionalOnMissingBean(HttpLongPollingDataChangedListener.class)
        public DataChangedListener httpLongPollingDataChangedListener(final HttpSyncProperties httpSyncProperties) {
            return new HttpLongPollingDataChangedListener(httpSyncProperties);
        }

        @Autowired
        void setDataChangedListener(DataChangeEventMulticaster multicaster, DataChangedListener httpLongPollingDataChangedListener) {
            multicaster.setListeners(httpLongPollingDataChangedListener);
        }
    }

    @Configuration
    @ConditionalOnProperty(name = "swallow.sync.zookeeper.enabled", havingValue = "true")
    @EnableConfigurationProperties(ZookeeperSyncProperties.class)
    static class ZookeeperListener {

        @Bean
        public ZkClient zkClient(ZookeeperSyncProperties zookeeperSyncProperties) {
            return new ZkClient(zookeeperSyncProperties.getZkServers(), zookeeperSyncProperties.getSessionTimeout(), zookeeperSyncProperties.getConnectionTimeout(), zookeeperSyncProperties.getZkSerializer(), zookeeperSyncProperties.getOperationRetryTimeout());
        }

        @Bean
        @ConditionalOnMissingBean(ZookeeperDataChangedListener.class)
        public DataChangedListener zookeeperDataChangedListener(final ObjectProvider<ZkClient> zkClient) {
            return new ZookeeperDataChangedListener(zkClient.getIfAvailable());
        }

        @Autowired
        void setDataChangedListener(DataChangeEventMulticaster multicaster, DataChangedListener httpLongPollingDataChangedListener) {
            multicaster.setListeners(httpLongPollingDataChangedListener);
        }
    }

}
