package com.future94.swallow.web.dubbo.common.cache;

import com.future94.swallow.common.dto.MetaDataRegisterDto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author weilai
 */
public class PathMetaDataCache {

    private static final Map<String, MetaDataRegisterDto> PATH_META_DATA_CACHE = new ConcurrentHashMap<>();

    public static void put(MetaDataRegisterDto dataRegisterDto) {
        PATH_META_DATA_CACHE.put(dataRegisterDto.getPath(), dataRegisterDto);
    }

    public static void remove(String path) {
        PATH_META_DATA_CACHE.remove(path);
    }

    public static MetaDataRegisterDto get(String path) {
        return PATH_META_DATA_CACHE.get(path);
    }
}
