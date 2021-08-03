package com.future94.swallow.data.client.nacos;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.future94.swallow.common.cache.PathMetaDataCache;
import com.future94.swallow.common.constants.SyncDataPathConstant;
import com.future94.swallow.common.dto.MetaDataRegisterDto;
import com.future94.swallow.common.utils.GsonUtils;
import com.future94.swallow.data.common.api.MetaDataSubscriber;
import com.future94.swallow.data.common.api.SyncMetaDataService;
import com.google.gson.JsonParseException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * @author weilai
 */
@Slf4j
public class NacosSyncMetaDataService implements SyncMetaDataService, Closeable {

    private final String nacosGroup;

    private final long timeoutMs;

    private final ConfigService configService;

    private final List<MetaDataSubscriber> metaDataSubscriberList;

    public NacosSyncMetaDataService(String nacosGroup, long timeoutMs, ConfigService configService, List<MetaDataSubscriber> metaDataSubscriberList) {
        this.nacosGroup = nacosGroup;
        this.timeoutMs = timeoutMs;
        this.configService = configService;
        this.metaDataSubscriberList = metaDataSubscriberList;
        this.init();
        this.start();
    }

    @SneakyThrows
    public void start() {
        configService.getConfigAndSignListener(SyncDataPathConstant.NACOS_METADATA_DATA_ID, nacosGroup, timeoutMs, new Listener() {

            @Override
            public void receiveConfigInfo(final String configInfo) {
                try {
                    syncMetadataConfigInfo(configInfo);
                } catch (JsonParseException e) {
                    log.error("sync metadata error:", e);
                }
            }

            /**
             * Use default executor.
             */
            @Override
            public Executor getExecutor() {
                return null;
            }
        });
    }

    @SneakyThrows
    private void init() {
        Optional.ofNullable(configService.getConfig(SyncDataPathConstant.NACOS_METADATA_DATA_ID, nacosGroup, timeoutMs)).ifPresent(this::syncMetadataConfigInfo);
    }

    private void syncMetadataConfigInfo(String configInfo) {
        final List<MetaDataRegisterDto> metaDataList = new ArrayList<>(GsonUtils.getInstance().toObjectMap(configInfo, MetaDataRegisterDto.class).values());
        if (!PathMetaDataCache.hasPath()) {
            refresh(metaDataList);
        } else {
            add(metaDataList.stream().filter(m -> !PathMetaDataCache.hasPath(m.getPath())).collect(Collectors.toList()));
            remove(PathMetaDataCache.getMetadataList().stream().filter(m -> ! metaDataList.contains(m)).map(MetaDataRegisterDto::getPath).collect(Collectors.toList()));
        }
    }

    private void refresh(List<MetaDataRegisterDto> metaDataList) {
        metaDataList.forEach(metaData -> metaDataSubscriberList.forEach(subscriber -> {
            // first clear metadata info
            subscriber.unSubscribe(metaData.getPath());
            // second recreate metadata info
            subscriber.onSubscribe(metaData);
        }));
    }

    private void add(List<MetaDataRegisterDto> onSubscribeList) {
        onSubscribeList.forEach(metaDataRegisterDto -> metaDataSubscriberList.forEach(metaDataSubscriber -> metaDataSubscriber.onSubscribe(metaDataRegisterDto)));
    }

    private void remove(List<String> unSubscribeList) {
        unSubscribeList.forEach(path -> metaDataSubscriberList.forEach(metaDataSubscriber -> metaDataSubscriber.unSubscribe(path)));
    }

    @SneakyThrows
    @Override
    public void close() throws IOException {
        if (configService != null) {
            configService.shutDown();
        }
    }
}
