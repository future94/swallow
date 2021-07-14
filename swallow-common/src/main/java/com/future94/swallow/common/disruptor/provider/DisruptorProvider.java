package com.future94.swallow.common.disruptor.provider;

import com.future94.swallow.common.disruptor.event.SwallowEvent;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

/**
 * @author weilai
 */
@Slf4j
public class DisruptorProvider<T> {

    private final RingBuffer<SwallowEvent<T>> ringBuffer;

    private final Disruptor<SwallowEvent<T>> disruptor;

    public DisruptorProvider(RingBuffer<SwallowEvent<T>> ringBuffer, Disruptor<SwallowEvent<T>> disruptor) {
        this.ringBuffer = ringBuffer;
        this.disruptor = disruptor;
    }

    public void onData(final Consumer<SwallowEvent<T>> function) {
        long position = ringBuffer.next();
        try {
            SwallowEvent<T> dataEvent = ringBuffer.get(position);
            function.accept(dataEvent);
            ringBuffer.publish(position);
        } catch (Exception ex) {
            log.error("ex", ex);
        }
    }

    public void shutdown() {
        if (null != disruptor) {
            disruptor.shutdown();
        }
    }

}
