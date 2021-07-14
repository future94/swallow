package com.future94.swallow.common.disruptor.consumer;

import com.future94.swallow.common.dto.ServerConfig;
import lombok.Data;

/**
 * @author weilai
 */
@Data
public abstract class SwallowConsumerExecutor<T> implements Runnable{

    private T data;

    public abstract void init(ServerConfig serverConfig);
}
