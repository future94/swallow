package com.future94.swallow.data.client.etcd;

import com.future94.swallow.common.constants.SyncDataPathConstant;
import com.future94.swallow.common.dto.MetaDataRegisterDto;
import com.future94.swallow.common.utils.GsonUtils;
import com.future94.swallow.data.common.api.MetaDataSubscriber;
import com.future94.swallow.data.common.api.SyncMetaDataService;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.io.Closeable;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author weilai
 */
@Slf4j
public class EtcdSyncMetaDataService implements SyncMetaDataService, Closeable {

    private final Client etcdClient;

    private final List<MetaDataSubscriber> metaDataSubscriberList;

    public EtcdSyncMetaDataService(Client etcdClient, List<MetaDataSubscriber> metaDataSubscriberList) {
        this.etcdClient = etcdClient;
        this.metaDataSubscriberList = metaDataSubscriberList;
        this.start();
    }

    public void start() {
        List<String> childrenList = getEtcdClientChildren();
        if (!CollectionUtils.isEmpty(childrenList)) {
            childrenList.forEach(children -> cacheMetaData(get(buildRealPath(children))));
        }
        subscribeChildChanges();
    }

    /**
     * get etcd all children list
     */
    @SneakyThrows
    private List<String> getEtcdClientChildren() {
        ByteSequence prefixByteSequence = ByteSequence.from(SyncDataPathConstant.ETCD_METADATA_PATH, StandardCharsets.UTF_8);
        GetOption getOption = GetOption.newBuilder().isPrefix(true).withSortField(GetOption.SortTarget.KEY).withSortOrder(GetOption.SortOrder.ASCEND).build();
        List<KeyValue> keyValues = etcdClient.getKVClient().get(prefixByteSequence, getOption).get().getKvs();
        return keyValues.stream().map(e -> getChildrenName(e.getKey().toString(StandardCharsets.UTF_8))).distinct().collect(Collectors.toList());
    }

    /**
     * subscribe etcd child change
     */
    private void subscribeChildChanges() {
        watchChildChange((updatePath, updateValue) -> cacheMetaData(get(updatePath)), this::unCacheMetaData);
    }

    public void watchChildChange(final BiConsumer<String, String> updateHandler, final Consumer<String> deleteHandler) {
        Watch.Listener listener = watch(updateHandler, deleteHandler);
        WatchOption option = WatchOption.newBuilder().isPrefix(true).build();
        etcdClient.getWatchClient().watch(ByteSequence.from(SyncDataPathConstant.ETCD_METADATA_PATH, StandardCharsets.UTF_8), option, listener);
    }

    private void cacheMetaData(final String dataString) {
        final MetaDataRegisterDto metaData = GsonUtils.getInstance().fromJson(dataString, MetaDataRegisterDto.class);
        Optional.ofNullable(metaData).ifPresent(data -> metaDataSubscriberList.forEach(e -> e.onSubscribe(metaData)));
    }

    private void unCacheMetaData(final String deletePath) {
        String path = getPath(getChildrenName(deletePath));
        Optional.ofNullable(path).ifPresent(data -> metaDataSubscriberList.forEach(e -> e.unSubscribe(path)));
    }

    /**
     *
     * @param fullPath  eg: /swallow/metadata/%2Ftest
     * @return          eg: %2Ftest
     */
    private String getChildrenName(final String fullPath) {
        return fullPath.substring(SyncDataPathConstant.ETCD_METADATA_PATH.length() + 1);
    }

    /**
     * @param children  eg: %2Ftest
     * @return          eg: /test
     */
    @SneakyThrows
    private String getPath(final String children) {
        return URLDecoder.decode(children, StandardCharsets.UTF_8.name());
    }

    /**
     * @param children  eg: %2Ftest
     * @return          eg: /swallow/metadata/%2Ftest
     */
    private String buildRealPath(final String children) {
        return String.join(SyncDataPathConstant.SEPARATOR, SyncDataPathConstant.ETCD_METADATA_PATH, children);
    }

    @SneakyThrows
    public String get(final String key) {
        List<KeyValue> keyValues = etcdClient.getKVClient().get(ByteSequence.from(key, StandardCharsets.UTF_8)).get().getKvs();
        return keyValues.isEmpty() ? null : keyValues.iterator().next().getValue().toString(StandardCharsets.UTF_8);
    }

    private Watch.Listener watch(final BiConsumer<String, String> updateHandler, final Consumer<String> deleteHandler) {
        return Watch.listener(response -> {
            if (log.isDebugEnabled()) {
                log.debug("receive etcd response :[{}]", response.toString());
            }
            for (WatchEvent event: response.getEvents()) {
                String path = event.getKeyValue().getKey().toString(StandardCharsets.UTF_8);
                String value = event.getKeyValue().getValue().toString(StandardCharsets.UTF_8);
                switch (event.getEventType()) {
                    case PUT:
                        if (log.isInfoEnabled()) {
                            log.info("receive etcd PUT event, path:{}, value:{}", path, value);
                        }
                        updateHandler.accept(path, value);
                        continue;
                    case DELETE:
                        if (log.isInfoEnabled()) {
                            log.info("receive etcd DELETE event, path:{}", path);
                        }
                        deleteHandler.accept(path);
                        continue;
                    case UNRECOGNIZED:
                        log.warn("ectd watch metadata unrecognized eventType, path:{}, value:{}", path, value);
                        continue;
                    default:
                }
            }
        });
    }

    @Override
    public void close() throws IOException {
        Optional.ofNullable(etcdClient).ifPresent(Client::close);
    }
}
