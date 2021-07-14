package com.future94.swallow.data.client.http.config;

import lombok.Data;

/**
 * @author weilai
 */
@Data
public class HttpConfig {

    /**
     * sync data center address.
     */
    private String serverAddr = "http://localhost:9507";

    /**
     * HTTP long rotation fail retry delay time in milliseconds.
     */
    private Integer retryDelayTime = 5000;

    /**
     * HTTP long rotation retry fail delay time in seconds.
     */
    private Integer failDelayTime = 180;

    /**
     * Set the http connect timeout in milliseconds.
     */
    private Integer connectionTimeout = 3000;

    /**
     * Set the http read timeout in milliseconds.
     */
    private Integer readTimeout = 90000;

    /**
     * Number of retries failed in HTTP long rotation.
     */
    private Integer maxRetry = 3;
}