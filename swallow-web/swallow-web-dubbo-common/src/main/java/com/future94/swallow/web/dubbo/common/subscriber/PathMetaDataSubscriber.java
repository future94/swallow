package com.future94.swallow.web.dubbo.common.subscriber;

import com.future94.swallow.common.dto.MetaDataRegisterDto;
import com.future94.swallow.data.common.api.MetaDataSubscriber;
import com.future94.swallow.web.dubbo.common.cache.PathMetaDataCache;

/**
 * @author weilai
 */
public class PathMetaDataSubscriber implements MetaDataSubscriber {

    @Override
    public void onSubscribe(MetaDataRegisterDto metaData) {
        PathMetaDataCache.put(metaData);
    }

    @Override
    public void unSubscribe(String path) {
        PathMetaDataCache.remove(path);
    }
}
