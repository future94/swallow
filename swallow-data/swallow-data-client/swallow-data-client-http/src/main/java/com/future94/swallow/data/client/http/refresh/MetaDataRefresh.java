package com.future94.swallow.data.client.http.refresh;

import com.future94.swallow.common.dto.ConfigData;
import com.future94.swallow.common.dto.MetaDataRegisterDto;
import com.future94.swallow.data.common.api.MetaDataSubscriber;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author weilai
 */
@Slf4j
@RequiredArgsConstructor
public class MetaDataRefresh implements DataRefresh {

    private ConfigData<MetaDataRegisterDto> cache;

    private final List<MetaDataSubscriber> metaDataSubscribers;

    private final Gson GSON = new Gson();

    @Override
    public Boolean refresh(final JsonObject data) {
        boolean updated = false;
        if (null != data) {
            ConfigData<MetaDataRegisterDto> result = fromJson(data);
            if (this.updateCacheIfNeed(result)) {
                updated = true;
                refresh(result.getData());
            }
        }
        return updated;
    }

    private ConfigData<MetaDataRegisterDto> fromJson(final JsonObject data) {
        return GSON.fromJson(data, new TypeToken<ConfigData<MetaDataRegisterDto>>() {
        }.getType());
    }

    private boolean updateCacheIfNeed(final ConfigData<MetaDataRegisterDto> newVal) {
        // first init cache
        if (cache == null) {
            cache = newVal;
            return true;
        }
        final ConfigData<MetaDataRegisterDto> oldVal = cache;
        if (StringUtils.equals(oldVal.getMd5(), newVal.getMd5())) {
            log.info("Get the same config, the metadata config cache will not be updated, md5:{}", oldVal.getMd5());
            return false;
        }
        // must compare the last update time
        if (oldVal.getLastModifyTime() >= newVal.getLastModifyTime()) {
            log.info("Last update time earlier than the current configuration, the meta config cache will not be updated");
            return false;
        }
        cache = newVal;
        log.info("update metadata config: {}", newVal);
        return true;
    }

    @Override
    public ConfigData<MetaDataRegisterDto> cacheConfigData() {
        return cache;
    }

    private void refresh(final List<MetaDataRegisterDto> data) {
        if (CollectionUtils.isEmpty(data)) {
            log.info("clear all metaData cache");
            metaDataSubscribers.forEach(MetaDataSubscriber::refresh);
        } else {
            data.forEach(metaData -> metaDataSubscribers.forEach(subscriber -> subscriber.onSubscribe(metaData)));
        }
    }
}
