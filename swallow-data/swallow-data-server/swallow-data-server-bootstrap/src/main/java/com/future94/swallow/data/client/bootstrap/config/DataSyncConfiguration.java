package com.future94.swallow.data.client.bootstrap.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.future94.swallow.data.client.bootstrap.listener.DataChangeEventMulticaster;
import com.future94.swallow.data.client.bootstrap.listener.DataChangedListener;
import com.future94.swallow.data.client.bootstrap.listener.http.HttpLongPollingDataChangedListener;
import com.future94.swallow.data.client.bootstrap.listener.nacos.NacosDataChangedListener;
import com.future94.swallow.data.client.bootstrap.listener.zookeeper.ZookeeperDataChangedListener;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;
import java.util.Properties;

import static com.alibaba.nacos.api.PropertyKeyConst.ACCESS_KEY;
import static com.alibaba.nacos.api.PropertyKeyConst.CLUSTER_NAME;
import static com.alibaba.nacos.api.PropertyKeyConst.CONFIG_LONG_POLL_TIMEOUT;
import static com.alibaba.nacos.api.PropertyKeyConst.CONFIG_RETRY_TIME;
import static com.alibaba.nacos.api.PropertyKeyConst.ENABLE_REMOTE_SYNC_CONFIG;
import static com.alibaba.nacos.api.PropertyKeyConst.ENCODE;
import static com.alibaba.nacos.api.PropertyKeyConst.ENDPOINT;
import static com.alibaba.nacos.api.PropertyKeyConst.ENDPOINT_PORT;
import static com.alibaba.nacos.api.PropertyKeyConst.MAX_RETRY;
import static com.alibaba.nacos.api.PropertyKeyConst.NAMESPACE;
import static com.alibaba.nacos.api.PropertyKeyConst.PASSWORD;
import static com.alibaba.nacos.api.PropertyKeyConst.SECRET_KEY;
import static com.alibaba.nacos.api.PropertyKeyConst.SERVER_ADDR;
import static com.alibaba.nacos.api.PropertyKeyConst.USERNAME;

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
        public HttpLongPollingDataChangedListener httpLongPollingDataChangedListener(final HttpSyncProperties httpSyncProperties) {
            return new HttpLongPollingDataChangedListener(httpSyncProperties);
        }

        @Autowired
        void setDataChangedListener(DataChangeEventMulticaster multicaster, HttpLongPollingDataChangedListener httpLongPollingDataChangedListener) {
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
        public ZookeeperDataChangedListener zookeeperDataChangedListener(final ObjectProvider<ZkClient> zkClient) {
            return new ZookeeperDataChangedListener(zkClient.getIfAvailable());
        }

        @Autowired
        void setDataChangedListener(DataChangeEventMulticaster multicaster, ZookeeperDataChangedListener zookeeperDataChangedListener) {
            multicaster.setListeners(zookeeperDataChangedListener);
        }
    }

    @Configuration
    @ConditionalOnProperty(name = "swallow.sync.nacos.enabled", havingValue = "true")
    @EnableConfigurationProperties(NacosSyncProperties.class)
    static class NacosListener {

        @Bean
        public ConfigService zkClient(NacosSyncProperties nacosProperties) throws NacosException {
            Properties properties = new Properties();
            properties.put(SERVER_ADDR, Objects.toString(nacosProperties.getServerAddr(), ""));
            properties.put(USERNAME, Objects.toString(nacosProperties.getUsername(), ""));
            properties.put(PASSWORD, Objects.toString(nacosProperties.getPassword(), ""));
            properties.put(ENCODE, Objects.toString(nacosProperties.getEncode(), ""));
            properties.put(NAMESPACE, Objects.toString(nacosProperties.getNamespace(), ""));
            properties.put(ACCESS_KEY, Objects.toString(nacosProperties.getAccessKey(), ""));
            properties.put(SECRET_KEY, Objects.toString(nacosProperties.getSecretKey(), ""));
            properties.put(CLUSTER_NAME, Objects.toString(nacosProperties.getClusterName(), ""));
            properties.put(MAX_RETRY, Objects.toString(nacosProperties.getMaxRetry(), ""));
            properties.put(CONFIG_LONG_POLL_TIMEOUT,
                    Objects.toString(nacosProperties.getConfigLongPollTimeout(), ""));
            properties.put(CONFIG_RETRY_TIME, Objects.toString(nacosProperties.getConfigRetryTime(), ""));
            properties.put(ENABLE_REMOTE_SYNC_CONFIG,
                    Objects.toString(nacosProperties.isEnableRemoteSyncConfig(), ""));
            String endpoint = Objects.toString(nacosProperties.getEndpoint(), "");
            if (endpoint.contains(":")) {
                int index = endpoint.indexOf(":");
                properties.put(ENDPOINT, endpoint.substring(0, index));
                properties.put(ENDPOINT_PORT, endpoint.substring(index + 1));
            } else {
                properties.put(ENDPOINT, endpoint);
            }
            return NacosFactory.createConfigService(properties);
        }

        @Bean
        @ConditionalOnMissingBean(ZookeeperDataChangedListener.class)
        public NacosDataChangedListener zookeeperDataChangedListener(final ObjectProvider<ConfigService> configService, ObjectProvider<NacosSyncProperties> nacosSyncProperties) {
            return new NacosDataChangedListener(configService.getIfAvailable(), nacosSyncProperties.getIfAvailable());
        }

        @Autowired
        void setDataChangedListener(DataChangeEventMulticaster multicaster, NacosDataChangedListener nacosDataChangedListener) {
            multicaster.setListeners(nacosDataChangedListener);
        }
    }

}
