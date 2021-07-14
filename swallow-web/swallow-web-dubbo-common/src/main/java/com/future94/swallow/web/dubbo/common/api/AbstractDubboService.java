package com.future94.swallow.web.dubbo.common.api;

import com.future94.swallow.web.dubbo.common.constants.Constants;
import com.future94.swallow.web.dubbo.common.utils.HttpParamConverter;
import com.future94.swallow.web.dubbo.common.utils.ResponseUtils;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * @author weilai
 */
public abstract class AbstractDubboService implements DubboService{

    @Override
    public Mono<Void> execute(ServerWebExchange exchange) {
        final ServerHttpRequest request = exchange.getRequest();
        MediaType mediaType = request.getHeaders().getContentType();
        ServerRequest serverRequest = ServerRequest.create(exchange, HandlerStrategies.withDefaults().messageReaders());
        if (MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) {
            return body(exchange, serverRequest);
        } else if (MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(mediaType)) {
            return formData(exchange, serverRequest);
        } else if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(mediaType)) {
            return multipartData(exchange, serverRequest);
        } else {
            return query(exchange, serverRequest);
        }
    }

    protected abstract Mono<Void> doExecute(ServerWebExchange exchange);

    public Mono<Void> buildResponse(final ServerWebExchange exchange) {
        return Mono.defer(() -> {
            final Object dubboResult = exchange.getAttribute(Constants.DUBBO_RESULT);
            final Object dubboError = exchange.getAttribute(Constants.DUBBO_ERROR);
            if (Objects.nonNull(dubboError)) {
                Object error = ResponseUtils.genericError((Throwable) dubboError);
                return ResponseUtils.result(exchange, error);
            }
            if (Objects.isNull(dubboResult)) {
                Object error = ResponseUtils.error(500, "error", null);
                return ResponseUtils.result(exchange, error);
            }
            Object success = ResponseUtils.success(200, "success", ResponseUtils.removeClass(dubboResult));
            return ResponseUtils.result(exchange, success);
        });
    }

    private Mono<Void> body(final ServerWebExchange exchange, final ServerRequest serverRequest) {
        return serverRequest.bodyToMono(String.class)
                .switchIfEmpty(Mono.defer(() -> Mono.just("")))
                .flatMap(body -> {
                    exchange.getAttributes().put(Constants.DUBBO_PARAMS, body);
                    return doExecute(exchange);
                });
    }

    private Mono<Void> formData(final ServerWebExchange exchange, final ServerRequest serverRequest) {
        return serverRequest.formData()
                .switchIfEmpty(Mono.defer(() -> Mono.just(new LinkedMultiValueMap<>())))
                .flatMap(map -> {
                    exchange.getAttributes().put(Constants.DUBBO_PARAMS, HttpParamConverter.ofFormData(map));
                    return doExecute(exchange);
                });
    }

    private Mono<Void> multipartData(final ServerWebExchange exchange, final ServerRequest serverRequest) {
        return serverRequest.multipartData()
                .switchIfEmpty(Mono.defer(() -> Mono.just(new LinkedMultiValueMap<>())))
                .flatMap(map -> {
                    exchange.getAttributes().put(Constants.DUBBO_PARAMS, HttpParamConverter.ofMultipartData(map));
                    return doExecute(exchange);
                });
    }

    private Mono<Void> query(final ServerWebExchange exchange, final ServerRequest serverRequest) {
        exchange.getAttributes().put(Constants.DUBBO_PARAMS,
                HttpParamConverter.ofString(serverRequest.uri().getQuery()));
        return doExecute(exchange);
    }
}
