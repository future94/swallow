package com.future94.swallow.client.dubbo.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author weilai
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DubboRpcExt implements Serializable {

    private static final long serialVersionUID = -8215106519502042254L;

    /**
     * 组
     */
    private String group;

    /**
     * 版本
     */
    private String version;

    /**
     * 负载策略
     */
    private String loadbalance;

    /**
     * 重试次数
     */
    private Integer retries;

    /**
     * 超时时间
     */
    private Integer timeout;
}
