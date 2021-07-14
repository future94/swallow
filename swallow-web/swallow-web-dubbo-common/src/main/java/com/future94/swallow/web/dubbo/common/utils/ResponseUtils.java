package com.future94.swallow.web.dubbo.common.utils;

import com.future94.swallow.common.dto.SwallowResponse;
import com.future94.swallow.framework.utils.SpringUtils;
import com.google.gson.Gson;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;

/**
 * @author weilai
 */
public class ResponseUtils {

    public static Mono<Void> result(final ServerWebExchange exchange, final Object result) {
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                .bufferFactory().wrap(Objects.requireNonNull(new Gson().toJson(result)).getBytes())));
    }

    public static Object removeClass(final Object object) {
        if (object instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) object;
            Object result = map.get("result");
            if (result instanceof Map) {
                Map<?, ?> resultMap = (Map<?, ?>) result;
                resultMap.remove("class");
            }
            map.remove("class");
        }
        return object;
    }

    public static Object success(int code, String message, Object data) {
        return SpringUtils.getInstance().getBean(SwallowResponse.class).success(code, message, data);
    }

    public static Object error(int code, String message, Object data) {
        return SpringUtils.getInstance().getBean(SwallowResponse.class).error(code, message, data);
    }

    public static Object genericError(Throwable throwable) {
        return SpringUtils.getInstance().getBean(SwallowResponse.class).genericError(throwable);
    }
}
