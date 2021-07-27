package com.future94.swallow.spring.boot.web.handler;

import com.future94.swallow.web.dubbo.common.api.DubboService;
import com.future94.swallow.common.cache.PathMetaDataCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author weilai
 */
@Slf4j
public class SwallowWebHandler implements HandlerInterceptor {

    private final DubboService dubboService;

    public SwallowWebHandler(DubboService dubboService) {
        this.dubboService = dubboService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        if (PathMetaDataCache.get(path) != null) {
            if (log.isDebugEnabled()) {
                log.debug("path [{}] dubbo metadata found, start processing.", path);
            }
            response.getOutputStream().print("web");
            return false;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("path [{}] dubbo metadata not found, trying to deal with dispatcherHandler", path);
            }
            return false;
        }
    }
}
