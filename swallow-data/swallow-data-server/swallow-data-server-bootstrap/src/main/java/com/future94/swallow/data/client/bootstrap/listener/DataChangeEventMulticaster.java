package com.future94.swallow.data.client.bootstrap.listener;

import com.future94.swallow.data.client.bootstrap.entity.MetaData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author weilai
 */
@Component
@RequiredArgsConstructor
public class DataChangeEventMulticaster implements ApplicationListener<DataChangedEvent>, InitializingBean {

    private final ApplicationContext applicationContext;

    private List<DataChangedListener> listeners;

    @Override
    public void afterPropertiesSet() throws Exception {
        Collection<DataChangedListener> changedListeners = applicationContext.getBeansOfType(DataChangedListener.class).values();
        this.listeners = Collections.unmodifiableList(new ArrayList<>(changedListeners));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onApplicationEvent(DataChangedEvent event) {
        for (DataChangedListener listener : this.listeners) {
            listener.onMetaDataChanged((List<MetaData>) event.getSource(), event.getEventType());
        }
    }
}
