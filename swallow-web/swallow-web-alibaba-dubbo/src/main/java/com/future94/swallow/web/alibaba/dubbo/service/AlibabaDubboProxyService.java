package com.future94.swallow.web.alibaba.dubbo.service;

import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.rpc.service.GenericException;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.future94.swallow.common.dto.MetaDataRegisterDto;
import com.future94.swallow.common.exception.SwallowException;
import com.future94.swallow.common.utils.GsonUtils;
import com.future94.swallow.web.alibaba.dubbo.cache.ReferencePathCache;
import com.future94.swallow.common.cache.PathMetaDataCache;
import com.future94.swallow.web.dubbo.common.constants.Constants;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author weilai
 */
@Slf4j
public class AlibabaDubboProxyService {

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
        Object result = StringUtils.EMPTY;
        try {
            Object invoke = genericService.$invoke(metaData.getMethodName(), pair.getLeft(), pair.getRight());
            if (Objects.nonNull(invoke)) {
                result = invoke;
            }
            exchange.getAttributes().put(Constants.DUBBO_RESULT, result);
        } catch (GenericException e) {
            exchange.getAttributes().put(Constants.DUBBO_ERROR, e);
        }
        final Object returnObj = result;
        return Mono.defer(() -> Mono.just(returnObj));
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
