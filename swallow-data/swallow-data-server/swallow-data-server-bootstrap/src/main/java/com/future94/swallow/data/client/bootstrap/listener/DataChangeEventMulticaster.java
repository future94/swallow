package com.future94.swallow.data.client.bootstrap.listener;

import com.future94.swallow.common.dto.MetaDataRegisterDto;
import com.future94.swallow.common.enums.DataEventTypeEnum;
import com.future94.swallow.data.client.bootstrap.convert.Converter;
import com.future94.swallow.data.client.bootstrap.entity.MetaData;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author weilai
 */
@Component
@RequiredArgsConstructor
public class DataChangeEventMulticaster implements ApplicationListener<DataChangedEvent>{

    private final ApplicationEventPublisher eventPublisher;

    private List<DataChangedListener> listeners = new LinkedList<>();

    public void setListeners(DataChangedListener listener) {
        this.listeners.add(listener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onApplicationEvent(DataChangedEvent event) {
        for (DataChangedListener listener : this.listeners) {
            listener.onMetaDataChanged((List<MetaDataRegisterDto>) event.getSource(), event.getEventType());
        }
    }

    public void publishEvent(MetaData metaData, DataEventTypeEnum eventType) {
        eventPublisher.publishEvent(new DataChangedEvent(Collections.singletonList(Converter.INSTANCE.toDto(metaData)), eventType));
    }

}
