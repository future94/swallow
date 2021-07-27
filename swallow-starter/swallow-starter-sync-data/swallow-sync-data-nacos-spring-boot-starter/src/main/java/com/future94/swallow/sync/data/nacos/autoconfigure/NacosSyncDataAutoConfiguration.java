package com.future94.swallow.sync.data.nacos.autoconfigure;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.future94.swallow.data.client.nacos.NacosSyncMetaDataService;
import com.future94.swallow.data.common.api.MetaDataSubscriber;
import com.future94.swallow.data.common.api.SyncMetaDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
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
@Slf4j
@Configuration
@ConditionalOnClass(NacosSyncMetaDataService.class)
@EnableConfigurationProperties(NacosProperties.class)
@ConditionalOnProperty(prefix = "swallow.sync.nacos", name = "server-addr")
public class NacosSyncDataAutoConfiguration {

    @Bean
    public ConfigService configService(NacosProperties nacosProperties) throws NacosException {
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
    public SyncMetaDataService zookeeperSyncMetaDataService(NacosProperties nacosProperties, ObjectProvider<ConfigService> configService, ObjectProvider<List<MetaDataSubscriber>> metaDataSubscriberList) {
        return new NacosSyncMetaDataService(nacosProperties.getGroup(), Long.parseLong(nacosProperties.getTimeoutMs()), configService.getIfAvailable(), metaDataSubscriberList.getIfAvailable(Collections::emptyList));
    }
}
