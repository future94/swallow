package com.future94.swallow.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @author weilai
 */
@Data
public class DubboRegisterConfig implements Serializable {

    private static final long serialVersionUID = 3831961477567395506L;

    /**
     * Register center address
     */
    private String address;

    /**
     * Username to login register center
     */
    private String username;

    /**
     * Password to login register center
     */
    private String password;

    /**
     * Default port for register center
     */
    private Integer port;

    /**
     * Protocol for register center
     */
    private String protocol;

    /**
     * Network transmission type
     */
    private String transporter;

    private String server;

    private String client;

    /**
     * Affects how traffic distributes among registries, useful when subscribing multiple registries, available options:
     * 1. zone-aware, a certain type of traffic always goes to one Registry according to where the traffic is originated.
     */
    private String cluster;

    /**
     * The region where the registry belongs, usually used to isolate traffics
     */
    private String zone;

    /**
     * The group the services registry in
     */
    private String group;

    private String version;

    /**
     * Request timeout in milliseconds for register center
     */
    private Integer timeout;

    /**
     * Session timeout in milliseconds for register center
     */
    private Integer session;

    /**
     * File for saving register center dynamic list
     */
    private String file;

    /**
     * Wait time before stop
     */
    private Integer wait;

    /**
     * Whether to check if register center is available when boot up
     */
    private Boolean check;

    /**
     * Whether to allow dynamic service to register on the register center
     */
    private Boolean dynamic;

    /**
     * Whether to export service on the register center
     */
    private Boolean register;

    /**
     * Whether allow to subscribe service on the register center
     */
    private Boolean subscribe;

    /**
     * The customized parameters
     */
    private Map<String, String> parameters;

    /**
     * Whether it's default
     */
    private Boolean isDefault;

    /**
     * Simple the registry. both useful for provider and consumer
     *
     * @since 2.7.0
     */
    private Boolean simplified;
    /**
     * After simplify the registry, should add some parameter individually. just for provider.
     * <p>
     * such as: extra-keys = A,b,c,d
     *
     * @since 2.7.0
     */
    private String extraKeys;

    /**
     * the address work as config center or not
     */
    private Boolean useAsConfigCenter;

    /**
     * the address work as remote metadata center or not
     */
    private Boolean useAsMetadataCenter;

    /**
     * list of rpc protocols accepted by this registry, for example, "dubbo,rest"
     */
    private String accepts;

    /**
     * Always use this registry first if set to true, useful when subscribe to multiple registries
     */
    private Boolean preferred;

    /**
     * Affects traffic distribution among registries, useful when subscribe to multiple registries
     * Take effect only when no preferred registry is specified.
     */
    private Integer weight;
}
