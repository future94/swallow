package com.future94.swallow.sync.data.nacos.autoconfigure;

import com.alibaba.nacos.api.config.ConfigService;
import com.future94.swallow.common.constants.SyncDataPathConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author weilai
 */
@Data
@ConfigurationProperties(prefix = "swallow.sync.nacos")
public class NacosProperties {

    /**
     * nacos config server address.
     */
    private String serverAddr;

    /**
     * the nacos authentication username.
     */
    private String username;

    /**
     * the nacos authentication password.
     */
    private String password;

    /**
     * encode for nacos config content.
     */
    private String encode;

    /**
     * namespace, separation configuration of different environments.
     */
    private String namespace;

    /**
     * nacos config group, group is config data meta info.
     */
    private String group = SyncDataPathConstant.NACOS_DEFAULT_GROUP;

    /**
     * access key for namespace.
     */
    private String accessKey;

    /**
     * secret key for namespace.
     */
    private String secretKey;

    /**
     * nacos config cluster name.
     */
    private String clusterName;

    /**
     * nacos maximum number of tolerable server reconnection errors.
     */
    private String maxRetry;

    /**
     * nacos get config long poll timeout.
     */
    private String configLongPollTimeout;

    /**
     * nacos get config failure retry time.
     */
    private String configRetryTime;

    /**
     * nacos read timeout
     */
    private String timeoutMs = "3000";

    /**
     * If you want to pull it yourself when the program starts to get the configuration
     * for the first time, and the registered Listener is used for future configuration
     * updates, you can keep the original code unchanged, just add the system parameter:
     * enableRemoteSyncConfig = "true" ( But there is network overhead); therefore we
     * recommend that you use {@link ConfigService#getConfigAndSignListener} directly.
     */
    private boolean enableRemoteSyncConfig = false;

    /**
     * endpoint for Nacos, the domain name of a service, through which the server address
     * can be dynamically obtained.
     */
    private String endpoint;

}
