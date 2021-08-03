package com.future94.swallow.data.client.zookeeper;

import com.future94.swallow.common.cache.PathMetaDataCache;
import com.future94.swallow.common.constants.SyncDataPathConstant;
import com.future94.swallow.common.dto.MetaDataRegisterDto;
import com.future94.swallow.common.utils.GsonUtils;
import com.future94.swallow.data.common.api.MetaDataSubscriber;
import com.future94.swallow.data.common.api.SyncMetaDataService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.util.CollectionUtils;

import java.io.Closeable;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author weilai
 */
@Slf4j
public class ZookeeperSyncMetaDataService implements SyncMetaDataService, Closeable {

    private final ZkClient zkClient;

    private final List<MetaDataSubscriber> metaDataSubscriberList;

    public ZookeeperSyncMetaDataService(ZkClient zkClient, List<MetaDataSubscriber> metaDataSubscriberList) {
        this.zkClient = zkClient;
        this.metaDataSubscriberList = metaDataSubscriberList;
        this.start();
    }

    private void start() {
        List<String> childrenList = zkClientGetChildren();
        if (!CollectionUtils.isEmpty(childrenList)) {
            childrenList.forEach(children -> {
                String realPath = SyncDataPathConstant.ZOOKEEPER_METADATA_PATH + SyncDataPathConstant.SEPARATOR + children;
                MetaDataRegisterDto metaData = null == zkClient.readData(realPath) ? null
                        : GsonUtils.getInstance().fromJson((String) zkClient.readData(realPath), MetaDataRegisterDto.class);
                cacheMetaData(metaData);
                subscribeMetaDataChanges(realPath);
            });
        }
        subscribeChildChanges();
    }

    /**
     * subscribe ZOOKEEPER_METADATA_PATH.
     * create need subscribe metadata path.
     */
    private void subscribeChildChanges() {
        zkClient.subscribeChildChanges(SyncDataPathConstant.ZOOKEEPER_METADATA_PATH, (parentPath, currentChildren) -> {
            if (!CollectionUtils.isEmpty(currentChildren)) {
                // The path needs to be added
                final List<String> addSubscribePath = needAddSubscribePath(currentChildren);
                addSubscribePath.stream().map(children -> {
                    final String realPath = parentPath + SyncDataPathConstant.SEPARATOR + children;
                    MetaDataRegisterDto metaData = null == zkClient.readData(realPath) ? null : GsonUtils.getInstance().fromJson((String) zkClient.readData(realPath), MetaDataRegisterDto.class);
                    cacheMetaData(metaData);
                    return realPath;
                }).forEach(this::subscribeMetaDataChanges);
            }
        });
    }

    /**
     * need create subscribe path
     * @param currentChildren zookeeper current children path
     * @return need add subscribe path
     */
    private List<String> needAddSubscribePath(final List<String> currentChildren) {
        if (!PathMetaDataCache.hasPath()) {
            return currentChildren;
        }
        return currentChildren.stream().filter(current -> !PathMetaDataCache.hasPath(current)).collect(Collectors.toList());
    }

    /**
     * subscribe metadata path changes
     * @param realPath metadata real path
     */
    private void subscribeMetaDataChanges(final String realPath) {
        zkClient.subscribeDataChanges(realPath, new IZkDataListener() {
            @Override
            public void handleDataChange(final String dataPath, final Object data) {
                cacheMetaData(GsonUtils.getInstance().fromJson(data.toString(), MetaDataRegisterDto.class));
            }

            @SneakyThrows
            @Override
            public void handleDataDeleted(final String dataPath) {
                final String realPath = dataPath.substring(SyncDataPathConstant.ZOOKEEPER_METADATA_PATH.length() + 1);
                unCacheMetaData(URLDecoder.decode(realPath, StandardCharsets.UTF_8.name()));
            }
        });
    }

    private void unCacheMetaData(final String path) {
        Optional.ofNullable(path).ifPresent(data -> metaDataSubscriberList.forEach(e -> e.unSubscribe(path)));
    }

    private void cacheMetaData(final MetaDataRegisterDto metaDataRegisterDto) {
        Optional.ofNullable(metaDataRegisterDto).ifPresent(data -> metaDataSubscriberList.forEach(e -> e.onSubscribe(metaDataRegisterDto)));
    }

    private List<String> zkClientGetChildren() {
        if (!zkClient.exists(SyncDataPathConstant.ZOOKEEPER_METADATA_PATH)) {
            zkClient.createPersistent(SyncDataPathConstant.ZOOKEEPER_METADATA_PATH, true);
        }
        return zkClient.getChildren(SyncDataPathConstant.ZOOKEEPER_METADATA_PATH);
    }

    @Override
    public void close() throws IOException {
        if (zkClient != null) {
            zkClient.close();
        }
    }
}
