package com.future94.swallow.sync.data.http.autoconfigure;

import com.future94.swallow.data.client.http.HttpSyncMetaDataService;
import com.future94.swallow.data.client.http.config.HttpConfig;
import com.future94.swallow.data.common.api.MetaDataSubscriber;
import com.future94.swallow.data.common.api.SyncMetaDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author weilai
 */
@Configuration
@ConditionalOnClass(HttpSyncMetaDataService.class)
@ConditionalOnProperty(prefix = "swallow.sync.http", name = "server-addr")
@Slf4j
public class HttpSyncDataAutoConfiguration {

    /**
     * Http sync data service.
     *
     * @param httpConfig        the http config
     * @param metaSubscribers   the meta subscribers
     * @return the sync data service
     */
    @Bean
    public SyncMetaDataService httpSyncDataService(final ObjectProvider<HttpConfig> httpConfig, final ObjectProvider<List<MetaDataSubscriber>> metaSubscribers) {
        log.info("you use http long pull sync metadata");
        return new HttpSyncMetaDataService(Objects.requireNonNull(httpConfig.getIfAvailable()), metaSubscribers.getIfAvailable(Collections::emptyList));
    }

    /**
     * Http config http config.
     * @return the http config
     */
    @Bean
    @ConfigurationProperties(prefix = "swallow.sync.http")
    public HttpConfig httpConfig() {
        return new HttpConfig();
    }
}
