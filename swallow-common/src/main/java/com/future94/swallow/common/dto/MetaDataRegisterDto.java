package com.future94.swallow.common.dto;

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
public class MetaDataRegisterDto implements Serializable {

    private static final long serialVersionUID = -7932407447848519876L;

    /**
     * 应用名字
     */
    private String appName;

    /**
     * 上下文路径
     */
    private String contextPath;

    /**
     * 请求路径
     */
    private String path;

    /**
     * 路径描述
     */
    private String pathDesc;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 对应调用的方法名称
     */
    private String methodName;

    /**
     * 参数类型
     */
    private String parameterTypes;

    /**
     * RPC扩展参数
     * @see DubboParamExtInfo
     */
    private String rpcExt;

    /**
     * 是否自动注册
     */
    private Boolean enabled;

    @Data
    public static class DubboParamExtInfo {

        private String group;

        private String version;

        private String loadbalance;

        private Integer retries;

        private Integer timeout;

        private String url;
    }
}
