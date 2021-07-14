package com.future94.swallow.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author weilai
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SwallowCommonResponse implements Serializable {

    private static final long serialVersionUID = -8678956536572887321L;

    private static final int SUCCESS = 200;

    private static final String SUCCESS_MSG = "success";

    private static final int ERROR = 500;

    private static final String ERROR_MSG = "error";

    private Integer code;

    private String message;

    private Object data;

    public static SwallowCommonResponse success(Object data) {
        return success(SUCCESS, SUCCESS_MSG, data);
    }

    public static SwallowCommonResponse success(String msg, Object data) {
        return success(SUCCESS, msg, data);
    }

    public static SwallowCommonResponse success(Integer code, String msg, Object data) {
        return new SwallowCommonResponse(code, msg, data);
    }

    public static SwallowCommonResponse error(Object data) {
        return error(ERROR, ERROR_MSG, data);
    }

    public static SwallowCommonResponse error(String msg, Object data) {
        return error(ERROR, msg, data);
    }

    public static SwallowCommonResponse error(Integer code, String msg, Object data) {
        return new SwallowCommonResponse(code, msg, data);
    }
}
