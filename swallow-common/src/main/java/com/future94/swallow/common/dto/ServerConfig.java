package com.future94.swallow.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @author weilai
 */
@Data
public class ServerConfig implements Serializable {

    private static final long serialVersionUID = 34288770198884518L;

    /**
     * app name.
     */
    private String appName;

    /**
     * request url context path.
     */
    private String contextPath;

    /**
     * sync data center address.
     */
    private List<String> serverList = Collections.singletonList("http://localhost:9507");
}
