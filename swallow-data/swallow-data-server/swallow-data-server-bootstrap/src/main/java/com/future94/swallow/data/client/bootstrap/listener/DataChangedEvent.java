package com.future94.swallow.data.client.bootstrap.listener;

import com.future94.swallow.common.enums.DataEventTypeEnum;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * @author weilai
 */
@Getter
public class DataChangedEvent extends ApplicationEvent {

    private DataEventTypeEnum eventType;

    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    public DataChangedEvent(List<?> source, DataEventTypeEnum eventType) {
        super(source);
        this.eventType = eventType;
    }

    @Override
    public List<?> getSource() {
        return (List<?>) super.getSource();
    }
}
