package com.future94.swallow.examples.apache.dubbo.webflux.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author weilai
 */
@Data
@Accessors(fluent = true)
public class CommonResponse {

    private int code;

    private String msg;

    private Object data;
}
