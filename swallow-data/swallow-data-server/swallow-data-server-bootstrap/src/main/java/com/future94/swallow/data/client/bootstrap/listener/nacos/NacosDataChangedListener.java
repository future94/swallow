package com.future94.swallow.data.client.bootstrap.listener.nacos;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.ConfigType;
import com.future94.swallow.common.constants.SyncDataPathConstant;
import com.future94.swallow.common.dto.MetaDataRegisterDto;
import com.future94.swallow.common.enums.DataEventTypeEnum;
import com.future94.swallow.common.utils.GsonUtils;
import com.future94.swallow.data.client.bootstrap.config.NacosSyncProperties;
import com.future94.swallow.data.client.bootstrap.listener.DataChangedListener;
import com.future94.swallow.data.client.bootstrap.service.MetaDataService;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * @author weilai
 */
@Slf4j
public class NacosDataChangedListener implements DataChangedListener, InitializingBean {

    private static final ConcurrentMap<String, MetaDataRegisterDto> META_DATA = Maps.newConcurrentMap();

    private final ConfigService configService;

    private final NacosSyncProperties nacosSyncProperties;

    @Resource
    private MetaDataService metaDataService;

    public NacosDataChangedListener(ConfigService configService, NacosSyncProperties nacosSyncProperties) {
        this.configService = configService;
        this.nacosSyncProperties = nacosSyncProperties;
    }

    @Override
    public void onMetaDataChanged(List<MetaDataRegisterDto> changed, DataEventTypeEnum eventType) {
        updateMetaDataMap(getConfig());
        switch (eventType) {
            case DELETE:
                changed.forEach(meta -> META_DATA.remove(meta.getPath()));
                break;
            case REFRESH:
            case MYSELF:
                Set<String> set = new HashSet<>(META_DATA.keySet());
                changed.forEach(meta -> {
                    set.remove(meta.getPath());
                    META_DATA.put(meta.getPath(), meta);
                });
                META_DATA.keySet().removeAll(set);
                break;
            default:
                // CREATE or UPDATE
                changed.forEach(meta -> {
                    META_DATA.values()
                            .stream()
                            .filter(md -> Objects.equals(md.getAppName(), meta.getAppName()) && Objects.equals(md.getPath(), meta.getPath()))
                            .forEach(md -> META_DATA.remove(md.getPath()));

                    META_DATA.put(meta.getPath(), meta);
                });
                break;
        }
        publishConfig();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // init create parent path
        if (Objects.isNull(configService.getConfig(SyncDataPathConstant.NACOS_METADATA_DATA_ID, nacosSyncProperties.getGroup(), Long.parseLong(nacosSyncProperties.getTimeoutMs())))) {
            this.onMetaDataChanged(metaDataService.findAll(), DataEventTypeEnum.REFRESH);
        }
    }

    @SneakyThrows
    private String getConfig() {
        String config = configService.getConfig(SyncDataPathConstant.NACOS_METADATA_DATA_ID, SyncDataPathConstant.NACOS_DEFAULT_GROUP, Long.parseLong(nacosSyncProperties.getTimeoutMs()));
        return StringUtils.hasLength(config) ? config : "{}";
    }

    @SneakyThrows
    private void publishConfig() {
        configService.publishConfig(SyncDataPathConstant.NACOS_METADATA_DATA_ID, SyncDataPathConstant.NACOS_DEFAULT_GROUP, GsonUtils.getInstance().toJson(NacosDataChangedListener.META_DATA), ConfigType.JSON.name());
    }

    private void updateMetaDataMap(final String configInfo) {
        JsonObject jo = GsonUtils.getInstance().fromJson(configInfo, JsonObject.class);
        Set<String> set = new HashSet<>(META_DATA.keySet());
        for (Map.Entry<String, JsonElement> e : jo.entrySet()) {
            set.remove(e.getKey());
            META_DATA.put(e.getKey(), GsonUtils.getInstance().fromJson(e.getValue(), MetaDataRegisterDto.class));
        }
        META_DATA.keySet().removeAll(set);
    }
}
