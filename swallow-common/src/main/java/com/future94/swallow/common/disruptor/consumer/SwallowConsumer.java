package com.future94.swallow.common.disruptor.consumer;

import com.future94.swallow.common.disruptor.event.SwallowEvent;
import com.future94.swallow.common.dto.ServerConfig;
import com.lmax.disruptor.WorkHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;

/**
 * @author weilai
 */
@Slf4j
public class SwallowConsumer<T> implements WorkHandler<SwallowEvent<T>> {

    private final ExecutorService executorService;

    private final SwallowConsumerFactory<T> consumerFactory;

    private final ServerConfig serverConfig;

    public SwallowConsumer(ExecutorService executorService, SwallowConsumerFactory<T> consumerFactory, ServerConfig serverConfig) {
        this.executorService = executorService;
        this.consumerFactory = consumerFactory;
        this.serverConfig = serverConfig;
    }

    @Override
    public void onEvent(SwallowEvent<T> swallowEvent) throws Exception {
        if (swallowEvent != null) {
            SwallowConsumerExecutor<T> swallowConsumerExecutor = consumerFactory.create();
            swallowConsumerExecutor.setData(swallowEvent.getData());
            swallowConsumerExecutor.init(serverConfig);
            executorService.execute(swallowConsumerExecutor);
        }
    }
}
