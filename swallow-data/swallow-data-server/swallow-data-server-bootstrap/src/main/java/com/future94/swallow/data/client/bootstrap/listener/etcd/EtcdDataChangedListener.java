package com.future94.swallow.data.client.bootstrap.listener.etcd;

import com.future94.swallow.common.constants.SyncDataPathConstant;
import com.future94.swallow.common.dto.MetaDataRegisterDto;
import com.future94.swallow.common.enums.DataEventTypeEnum;
import com.future94.swallow.common.utils.GsonUtils;
import com.future94.swallow.data.client.bootstrap.listener.DataChangedListener;
import com.future94.swallow.data.client.bootstrap.service.MetaDataService;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.options.GetOption;
import lombok.SneakyThrows;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author weilai
 */
public class EtcdDataChangedListener implements DataChangedListener, InitializingBean {

    private final Client etcdClient;

    @Resource
    private MetaDataService metaDataService;

    public EtcdDataChangedListener(Client etcdClient) {
        this.etcdClient = etcdClient;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        GetOption option = GetOption.newBuilder().isPrefix(true).build();
        List<KeyValue> keyValues = etcdClient.getKVClient().get(ByteSequence.from(SyncDataPathConstant.ETCD_METADATA_PATH, StandardCharsets.UTF_8), option).get().getKvs();
        if (keyValues.isEmpty()) {
            this.onMetaDataChanged(metaDataService.findAll(), DataEventTypeEnum.REFRESH);
        }
    }

    @SneakyThrows
    @Override
    public void onMetaDataChanged(List<MetaDataRegisterDto> changed, DataEventTypeEnum eventType) {
        for (MetaDataRegisterDto data : changed) {
            String metaDataPath = String.join(SyncDataPathConstant.SEPARATOR, SyncDataPathConstant.ETCD_METADATA_PATH, URLEncoder.encode(data.getPath(), "UTF-8"));
            // delete
            if (DataEventTypeEnum.DELETE == eventType) {
                etcdClient.getKVClient().delete(ByteSequence.from(metaDataPath, StandardCharsets.UTF_8));
                continue;
            }
            // create or update
            updateNode(metaDataPath, data);
        }
    }

    @SneakyThrows
    private void updateNode(final String metaDataPath, final Object data) {
        etcdClient.getKVClient().put(ByteSequence.from(metaDataPath, StandardCharsets.UTF_8), ByteSequence.from(GsonUtils.getInstance().toJson(data), StandardCharsets.UTF_8)).get();
    }
}
