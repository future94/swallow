package com.future94.swallow.data.client.bootstrap.listener.zookeeper;

import com.future94.swallow.common.constants.SyncDataPathConstant;
import com.future94.swallow.common.dto.MetaDataRegisterDto;
import com.future94.swallow.common.enums.DataEventTypeEnum;
import com.future94.swallow.common.utils.GsonUtils;
import com.future94.swallow.data.client.bootstrap.listener.DataChangedListener;
import com.future94.swallow.data.client.bootstrap.service.MetaDataService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.util.List;

/**
 * @author weilai
 */
@Slf4j
public class ZookeeperDataChangedListener implements DataChangedListener, InitializingBean {

    private final ZkClient zkClient;

    @Resource
    private MetaDataService metaDataService;

    public ZookeeperDataChangedListener(ZkClient zkClient) {
        this.zkClient = zkClient;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // init create parent path
        if (!zkClient.exists(SyncDataPathConstant.ZOOKEEPER_METADATA_PATH)) {
            this.onMetaDataChanged(metaDataService.findAll(), DataEventTypeEnum.REFRESH);
        }
    }

    @SneakyThrows
    @Override
    public void onMetaDataChanged(List<MetaDataRegisterDto> changed, DataEventTypeEnum eventType) {
        for (MetaDataRegisterDto data : changed) {
            String metaDataPath = String.join(SyncDataPathConstant.SEPARATOR, SyncDataPathConstant.ZOOKEEPER_METADATA_PATH, URLEncoder.encode(data.getPath(), "UTF-8"));
            // delete
            if (eventType == DataEventTypeEnum.DELETE) {
                deleteZkPath(metaDataPath);
                continue;
            }
            // create or update
            saveZkNode(metaDataPath, data);
        }
    }

    private void saveZkNode(final String path, final Object data) {
        if (!zkClient.exists(path)) {
            zkClient.createPersistent(path, true);
        }
        zkClient.writeData(path, null == data ? "" : GsonUtils.getInstance().toJson(data));
    }

    private void deleteZkPath(final String path) {
        if (zkClient.exists(path)) {
            zkClient.delete(path);
        }
    }
}
