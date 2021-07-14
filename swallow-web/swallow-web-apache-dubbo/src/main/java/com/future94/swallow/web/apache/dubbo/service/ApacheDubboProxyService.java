package com.future94.swallow.web.apache.dubbo.service;

import com.future94.swallow.common.dto.MetaDataRegisterDto;
import com.future94.swallow.common.exception.SwallowException;
import com.future94.swallow.common.utils.GsonUtils;
import com.future94.swallow.web.apache.dubbo.cache.ReferencePathCache;
import com.future94.swallow.web.dubbo.common.cache.PathMetaDataCache;
import com.future94.swallow.web.dubbo.common.constants.Constants;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * @author weilai
 */
@Slf4j
public class ApacheDubboProxyService {

    /**
     * Generic invoker object.
     *
     * @param body     the body
     * @param exchange the exchange
     * @return the object
     * @throws SwallowException exception
     */
    public Mono<Object> genericInvoker(final String body, final ServerWebExchange exchange) throws SwallowException {
        String path = exchange.getRequest().getURI().getPath();
        MetaDataRegisterDto metaData = PathMetaDataCache.get(path);
        assert metaData != null;
        ReferenceConfig<GenericService> reference = ReferencePathCache.get(path);
        if (Objects.isNull(reference) || StringUtils.isEmpty(reference.getInterface())) {
            ReferencePathCache.invalidate(metaData.getPath());
            reference = ReferencePathCache.initRef(metaData);
        }
        GenericService genericService = reference.get();
        Pair<String[], Object[]> pair = buildParameters(body, metaData.getParameterTypes());
        CompletableFuture<Object> future = genericService.$invokeAsync(metaData.getMethodName(), pair.getLeft(), pair.getRight());
        return Mono.fromFuture(future.thenApply(ret -> {
            if (Objects.isNull(ret)) {
                ret = StringUtils.EMPTY;
            }
            exchange.getAttributes().put(Constants.DUBBO_RESULT, ret);
            return ret;
        }).exceptionally(throwable -> {
            exchange.getAttributes().put(Constants.DUBBO_ERROR, throwable.getCause());
            return StringUtils.EMPTY;
        }));
    }

    private static boolean dubboBodyIsEmpty(final String body) {
        return null == body || "".equals(body) || "{}".equals(body) || "null".equals(body);
    }

    private static Pair<String[], Object[]> buildParameters(final String body, final String parameterTypes) {
        if (dubboBodyIsEmpty(body)) {
            return new ImmutablePair<>(new String[]{}, new Object[]{});
        }
        String[] parameter = StringUtils.split(parameterTypes, ",");
        if (parameter.length == 1 && !isBaseType(parameter[0])) {
            return buildSingleParameter(body, parameterTypes);
        }
        Map<String, Object> paramMap = GsonUtils.getInstance().toObjectMap(body);
        List<Object> list = new LinkedList<>();
        for (String key : paramMap.keySet()) {
            Object obj = paramMap.get(key);
            if (obj instanceof JsonObject) {
                list.add(GsonUtils.getInstance().convertToMap(obj.toString()));
            } else if (obj instanceof JsonArray) {
                list.add(GsonUtils.getInstance().fromList(obj.toString(), Object.class));
            } else {
                list.add(obj);
            }
        }
        Object[] objects = list.toArray();
        return new ImmutablePair<>(parameter, objects);
    }

    private static Pair<String[], Object[]> buildSingleParameter(final String body, final String parameterTypes) {
        final Map<String, Object> paramMap = GsonUtils.getInstance().toObjectMap(body);
        for (String key : paramMap.keySet()) {
            Object obj = paramMap.get(key);
            if (obj instanceof JsonObject) {
                paramMap.put(key, GsonUtils.getInstance().convertToMap(obj.toString()));
            } else if (obj instanceof JsonArray) {
                paramMap.put(key, GsonUtils.getInstance().fromList(obj.toString(), Object.class));
            } else {
                paramMap.put(key, obj);
            }
        }
        return new ImmutablePair<>(new String[]{parameterTypes}, new Object[]{paramMap});
    }

    private static boolean isBaseType(final String paramType) {
        return paramType.startsWith("java") || paramType.startsWith("[Ljava");
    }
}
