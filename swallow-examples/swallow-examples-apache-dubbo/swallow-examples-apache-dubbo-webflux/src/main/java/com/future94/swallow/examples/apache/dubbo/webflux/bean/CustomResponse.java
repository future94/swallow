package com.future94.swallow.examples.apache.dubbo.webflux.bean;

import com.future94.swallow.common.dto.SwallowResponse;
import com.future94.swallow.examples.apache.dubbo.webflux.dto.CommonResponse;
import org.apache.dubbo.rpc.service.GenericException;
import org.springframework.stereotype.Component;

/**
 * @author weilai
 */
@Component
public class CustomResponse implements SwallowResponse<CommonResponse> {

    @Override
    public CommonResponse success(int code, String message, Object data) {
        return new CommonResponse().code(code).msg(message).data(data);
    }

    @Override
    public CommonResponse error(int code, String message, Object data) {
        return new CommonResponse().code(code).msg(message).data(data);
    }

    @Override
    public CommonResponse genericError(Throwable throwable) {
        if (throwable instanceof GenericException) {
            GenericException genericException = (GenericException) throwable;
            return new CommonResponse().code(500).msg(genericException.getExceptionClass()).data(genericException.getExceptionMessage());
        } else {
            return new CommonResponse().code(500).msg(throwable.getMessage());
        }
    }
}
