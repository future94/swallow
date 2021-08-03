package com.future94.swallow.sync.data.etcd.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author weilai
 */
@Data
@ConfigurationProperties(prefix = "swallow.sync.etcd")
public class EtcdProperties {

    private String endpoints = "http://127.0.0.1:2379";
}
