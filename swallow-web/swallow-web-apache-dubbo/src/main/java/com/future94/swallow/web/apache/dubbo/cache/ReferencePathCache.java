package com.future94.swallow.web.apache.dubbo.cache;

import com.future94.swallow.common.dto.MetaDataRegisterDto;
import com.future94.swallow.common.exception.SwallowException;
import com.future94.swallow.common.utils.GsonUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.Weigher;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.rpc.service.GenericService;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * @author weilai
 */
@Slf4j
public class ReferencePathCache {

    private static RegistryConfig registerConfig;

    private static ApplicationConfig applicationConfig;

    private static final LoadingCache<String, ReferenceConfig<GenericService>> GENERIC_CACHE = CacheBuilder.newBuilder()
            .maximumWeight(3000)
            .weigher( (Weigher<String, ReferenceConfig<GenericService>>) (string, referenceConfig) -> getSize())
            .removalListener( listener -> {
                ReferenceConfig<GenericService> config = listener.getValue();
                if (config != null) {
                    try {
                        Class<?> cz = config.getClass();
                        Field field = cz.getDeclaredField("ref");
                        field.setAccessible(true);
                        // 配置更改后，Dubbo 销毁实例，但不会清空实例。如果不处理，重新初始化时会得到 NULL 并导致 NULL 指针问题。
                        field.set(config, null);
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        log.error("modify ref have exception", e);
                    }
                }
            })
            .build(new CacheLoader<String, ReferenceConfig<GenericService>>() {
                @Override
                public ReferenceConfig<GenericService> load(final String key) {
                    return new ReferenceConfig<>();
                }
            });

    public static void init(RegistryConfig config) {
        if (registerConfig == null) {
            registerConfig = config;
        }
        if (applicationConfig == null) {
            applicationConfig = new ApplicationConfig("swallow-proxy-application");
        }
    }

    public static ReferenceConfig<GenericService> initRef(final MetaDataRegisterDto metaData) {
        try {
            ReferenceConfig<GenericService> referenceConfig = GENERIC_CACHE.get(metaData.getPath());
            if (StringUtils.isNoneBlank(referenceConfig.getInterface())) {
                return referenceConfig;
            }
        } catch (ExecutionException e) {
            log.error("init dubbo ref ex:{}", e.getMessage());
        }
        return build(metaData);

    }

    public static ReferenceConfig<GenericService> get(String path) {
        try {
            return GENERIC_CACHE.get(path);
        } catch (ExecutionException e) {
            throw new SwallowException(e);
        }
    }

    /**
     * 作废
     * @param path request uri.
     */
    public static void invalidate(String path) {
        GENERIC_CACHE.invalidate(path);
    }

    private static int getSize() {
        return (int) GENERIC_CACHE.size();
    }

    public static ReferenceConfig<GenericService> build(final MetaDataRegisterDto metaData) {
        ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
        reference.setRegistry(registerConfig);
        reference.setGeneric("true");
        reference.setApplication(applicationConfig);
        reference.setInterface(metaData.getServiceName());
        reference.setProtocol("dubbo");
        String rpcExt = metaData.getRpcExt();
        MetaDataRegisterDto.DubboParamExtInfo dubboParamExtInfo = GsonUtils.getInstance().fromJson(rpcExt, MetaDataRegisterDto.DubboParamExtInfo.class);
        if (Objects.nonNull(dubboParamExtInfo)) {
            if (StringUtils.isNoneBlank(dubboParamExtInfo.getVersion())) {
                reference.setVersion(dubboParamExtInfo.getVersion());
            }
            if (StringUtils.isNoneBlank(dubboParamExtInfo.getGroup())) {
                reference.setGroup(dubboParamExtInfo.getGroup());
            }
            if (StringUtils.isNoneBlank(dubboParamExtInfo.getLoadbalance())) {
                final String loadBalance = dubboParamExtInfo.getLoadbalance();
                reference.setLoadbalance(buildLoadBalanceName(loadBalance));
            }
            if (StringUtils.isNoneBlank(dubboParamExtInfo.getUrl())) {
                reference.setUrl(dubboParamExtInfo.getUrl());
            }
            Optional.ofNullable(dubboParamExtInfo.getTimeout()).ifPresent(reference::setTimeout);
            Optional.ofNullable(dubboParamExtInfo.getRetries()).ifPresent(reference::setRetries);
        }
        try {
            Object obj = reference.get();
            if (obj != null) {
                log.info("init apache dubbo reference success there meteData is :{}", metaData.toString());
                GENERIC_CACHE.put(metaData.getPath(), reference);
            }
        } catch (Exception e) {
            log.error("init apache dubbo reference ex:{}", e.getMessage());
        }
        return reference;
    }

    private static String buildLoadBalanceName(final String loadBalance) {
        if ("hash".equals(loadBalance) || "consistenthash".equals(loadBalance)) {
            return "consistenthash";
        }
        if ("roundRobin".equals(loadBalance)) {
            return "roundrobin";
        }
        return loadBalance;
    }
}
