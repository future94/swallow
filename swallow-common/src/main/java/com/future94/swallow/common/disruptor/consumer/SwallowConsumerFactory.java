package com.future94.swallow.common.disruptor.consumer;

/**
 * @author weilai
 */
public interface SwallowConsumerFactory<T> {

    SwallowConsumerExecutor<T> create();
}
