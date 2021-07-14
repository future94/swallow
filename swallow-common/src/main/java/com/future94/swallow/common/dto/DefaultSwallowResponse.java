package com.future94.swallow.common.dto;

import com.future94.swallow.common.exception.SwallowException;

/**
 * @author weilai
 */
public class DefaultSwallowResponse implements SwallowResponse<SwallowCommonResponse> {

    @Override
    public SwallowCommonResponse success(int code, String message, Object data) {
        return SwallowCommonResponse.success(code, message, data);
    }

    @Override
    public SwallowCommonResponse error(int code, String message, Object data) {
        return SwallowCommonResponse.error(code, message, data);
    }

    @Override
    public SwallowCommonResponse genericError(Throwable throwable){
        throw new SwallowException(throwable);
    }
}
