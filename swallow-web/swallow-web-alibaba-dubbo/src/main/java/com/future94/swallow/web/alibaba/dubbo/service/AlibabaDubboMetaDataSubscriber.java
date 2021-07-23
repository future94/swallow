package com.future94.swallow.web.alibaba.dubbo.service;

import com.future94.swallow.common.dto.MetaDataRegisterDto;
import com.future94.swallow.data.common.api.MetaDataSubscriber;
import com.future94.swallow.web.alibaba.dubbo.cache.ReferencePathCache;
import com.google.common.collect.Maps;

import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

/**
 * @author weilai
 */
public class AlibabaDubboMetaDataSubscriber implements MetaDataSubscriber {

    private static final ConcurrentMap<String, MetaDataRegisterDto> META_DATA = Maps.newConcurrentMap();

    @Override
    public void onSubscribe(MetaDataRegisterDto metaData) {
        MetaDataRegisterDto exist = META_DATA.get(metaData.getPath());
        if (Objects.isNull(exist) || Objects.isNull(ReferencePathCache.get(metaData.getPath()))) {
            // The first initialization
            ReferencePathCache.initRef(metaData);
        } else {
            // There are updates, which only support the update of four properties of serviceName rpcExt parameterTypes methodName,
            // because these four properties will affect the call of Dubbo;
            if (!Objects.equals(metaData.getServiceName(), exist.getServiceName())
                    || !Objects.equals(metaData.getRpcExt(), exist.getRpcExt())
                    || !Objects.equals(metaData.getParameterTypes(), exist.getParameterTypes())
                    || !Objects.equals(metaData.getMethodName(), exist.getMethodName())) {
                ReferencePathCache.build(metaData);
            }
        }
        META_DATA.put(metaData.getPath(), metaData);
    }

    @Override
    public void unSubscribe(String path) {
        ReferencePathCache.invalidate(path);
        META_DATA.remove(path);
    }
}
