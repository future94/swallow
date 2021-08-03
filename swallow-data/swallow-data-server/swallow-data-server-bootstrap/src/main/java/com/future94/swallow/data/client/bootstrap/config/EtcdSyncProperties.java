package com.future94.swallow.data.client.bootstrap.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author weilai
 */
@Data
@ConfigurationProperties(prefix = "swallow.sync.etcd")
public class EtcdSyncProperties {

    private String endpoints;
}
