package com.future94.swallow.common.disruptor;

import com.future94.swallow.common.disruptor.consumer.SwallowConsumer;
import com.future94.swallow.common.disruptor.consumer.SwallowConsumerFactory;
import com.future94.swallow.common.disruptor.event.SwallowEvent;
import com.future94.swallow.common.disruptor.event.SwallowEventFactory;
import com.future94.swallow.common.disruptor.provider.DisruptorProvider;
import com.future94.swallow.common.dto.ServerConfig;
import com.future94.swallow.common.thread.SwallowThreadFactory;
import com.lmax.disruptor.IgnoreExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author weilai
 */
public class DisruptorProviderManage<T> {

    private static final Integer DEFAULT_RING_BUFFER_SIZE = 1024 << 1;

    private static final Integer DEFAULT_CONSUMER_SIZE = Runtime.getRuntime().availableProcessors() << 1;

    private final Integer ringBufferSize;

    private final Integer consumerSize;

    private final ServerConfig serverConfig;

    private ExecutorService executorService;

    private DisruptorProvider<T> provider;

    private SwallowConsumerFactory<T> consumerFactory;

    public DisruptorProviderManage(SwallowConsumerFactory<T> consumerFactory, ServerConfig serverConfig) {
        this(DEFAULT_RING_BUFFER_SIZE, DEFAULT_CONSUMER_SIZE, consumerFactory, serverConfig);
    }

    public DisruptorProviderManage(Integer ringBufferSize, Integer consumerSize, SwallowConsumerFactory<T> consumerFactory, ServerConfig serverConfig) {
        this.ringBufferSize = ringBufferSize;
        this.consumerSize = consumerSize;
        this.serverConfig = serverConfig;
        this.consumerFactory = consumerFactory;
        this.executorService = new ThreadPoolExecutor(consumerSize, consumerSize, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), SwallowThreadFactory.create("swallow-disruptor-consumer-"), new ThreadPoolExecutor.AbortPolicy());
    }

    @SuppressWarnings("unchecked")
    public void startup() {
        Disruptor<SwallowEvent<T>> disruptor = new Disruptor<>(
                new SwallowEventFactory<>(),
                ringBufferSize,
                SwallowThreadFactory.create("swallow-disruptor-provider-"),
                ProducerType.MULTI,
                new YieldingWaitStrategy()
        );
        SwallowConsumer<T>[] swallowConsumers = new SwallowConsumer[consumerSize];
        for (int i = 0; i < consumerSize; i++) {
            swallowConsumers[i] = new SwallowConsumer<>(executorService, consumerFactory, serverConfig);
        }
        disruptor.handleEventsWithWorkerPool(swallowConsumers);
        disruptor.setDefaultExceptionHandler(new IgnoreExceptionHandler());
        disruptor.start();
        RingBuffer<SwallowEvent<T>> ringBuffer = disruptor.getRingBuffer();
        provider = new DisruptorProvider<>(ringBuffer, disruptor);
    }

    public DisruptorProvider<T> getProvider() {
        return provider;
    }

}
