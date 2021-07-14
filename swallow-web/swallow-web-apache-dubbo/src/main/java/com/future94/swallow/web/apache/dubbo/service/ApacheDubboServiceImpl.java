package com.future94.swallow.web.apache.dubbo.service;

import com.future94.swallow.web.dubbo.common.api.AbstractDubboService;
import com.future94.swallow.web.dubbo.common.constants.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author weilai
 */
@Slf4j
public class ApacheDubboServiceImpl extends AbstractDubboService {

    private final ApacheDubboProxyService dubboProxyService;

    public ApacheDubboServiceImpl(ApacheDubboProxyService dubboProxyService) {
        this.dubboProxyService = dubboProxyService;
    }

    @Override
    protected Mono<Void> doExecute(ServerWebExchange exchange) {
        String body = exchange.getAttribute(Constants.DUBBO_PARAMS);
        final Mono<Object> result = dubboProxyService.genericInvoker(body, exchange);
        return result.then(buildResponse(exchange));
    }
}
