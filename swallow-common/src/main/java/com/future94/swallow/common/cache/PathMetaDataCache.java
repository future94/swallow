package com.future94.swallow.common.cache;

import com.future94.swallow.common.dto.MetaDataRegisterDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public static boolean hasPath() {
        return !PATH_META_DATA_CACHE.isEmpty();
    }

    public static boolean hasPath(String path) {
        return PATH_META_DATA_CACHE.containsKey(path);
    }

    public static Set<String> getPathSet() {
        return PATH_META_DATA_CACHE.keySet();
    }

    public static List<MetaDataRegisterDto> getMetadataList() {
        return new ArrayList<>(PATH_META_DATA_CACHE.values());
    }
}
