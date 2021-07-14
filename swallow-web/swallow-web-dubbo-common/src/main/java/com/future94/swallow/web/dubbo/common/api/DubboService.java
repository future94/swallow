package com.future94.swallow.web.dubbo.common.api;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author weilai
 */
public interface DubboService {

    Mono<Void> execute(ServerWebExchange exchange);
}
