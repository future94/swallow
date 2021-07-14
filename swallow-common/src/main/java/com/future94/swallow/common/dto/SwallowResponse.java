package com.future94.swallow.common.dto;

/**
 * @author weilai
 */
public interface SwallowResponse<T> {

    T success(int code, String message, Object data);

    T error(int code, String message, Object data);

    T genericError(Throwable throwable);
}
