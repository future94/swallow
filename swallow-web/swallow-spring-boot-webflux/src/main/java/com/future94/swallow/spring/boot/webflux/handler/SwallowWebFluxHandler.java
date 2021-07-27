
package com.future94.swallow.spring.boot.webflux.handler;

import com.future94.swallow.web.dubbo.common.api.DubboService;
import com.future94.swallow.common.cache.PathMetaDataCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebHandler;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * @author weilai
 */
@Slf4j
public class SwallowWebFluxHandler implements WebHandler {

    private DubboService dubboService;

    private DispatcherHandler dispatcherHandler;

    public SwallowWebFluxHandler(DubboService dubboService, DispatcherHandler dispatcherHandler) {
        this.dubboService = dubboService;
        this.dispatcherHandler = dispatcherHandler;
    }

    @Override
    @NonNull
    public Mono<Void> handle(@NonNull ServerWebExchange serverWebExchange) {
        String path = serverWebExchange.getRequest().getURI().getPath();
        if (PathMetaDataCache.get(path) != null) {
            if (log.isDebugEnabled()) {
                log.debug("path [{}] dubbo metadata found, start processing.", path);
            }
            int threads = Math.max((Runtime.getRuntime().availableProcessors() << 1) + 1, 16);
            Scheduler scheduler = Schedulers.newParallel("swallow-work-threads", threads);
            return Mono.defer( () -> dubboService.execute(serverWebExchange).subscribeOn(scheduler));
        } else {
            if (log.isDebugEnabled()) {
                log.debug("path [{}] dubbo metadata not found, trying to deal with dispatcherHandler", path);
            }
            return dispatcherHandler.handle(serverWebExchange);
        }

    }
}
