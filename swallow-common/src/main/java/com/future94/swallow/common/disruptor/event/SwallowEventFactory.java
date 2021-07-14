package com.future94.swallow.common.disruptor.event;

import com.lmax.disruptor.EventFactory;

/**
 * @author weilai
 */
public class SwallowEventFactory<T> implements EventFactory<SwallowEvent<T>> {

    @Override
    public SwallowEvent<T> newInstance() {
        return new SwallowEvent<>();
    }
}
