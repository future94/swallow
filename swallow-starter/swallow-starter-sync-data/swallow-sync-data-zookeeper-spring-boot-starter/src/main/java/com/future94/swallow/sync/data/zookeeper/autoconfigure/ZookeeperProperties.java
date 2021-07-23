package com.future94.swallow.sync.data.zookeeper.autoconfigure;

import lombok.Data;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author weilai
 */
@Data
@ConfigurationProperties(prefix = "swallow.sync.zookeeper")
public class ZookeeperProperties {

    private String zkServers = "127.0.0.1:2181";

    private Integer sessionTimeout = 30000;

    private Integer connectionTimeout = Integer.MAX_VALUE;

    private ZkSerializer zkSerializer = new SerializableSerializer();

    private Long operationRetryTimeout = -1L;
}
