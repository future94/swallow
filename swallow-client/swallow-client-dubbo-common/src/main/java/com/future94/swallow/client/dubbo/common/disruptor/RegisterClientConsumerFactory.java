package com.future94.swallow.client.dubbo.common.disruptor;

import com.future94.swallow.common.disruptor.consumer.SwallowConsumerExecutor;
import com.future94.swallow.common.disruptor.consumer.SwallowConsumerFactory;
import com.future94.swallow.common.dto.MetaDataRegisterDto;

/**
 * @author weilai
 */
public class RegisterClientConsumerFactory implements SwallowConsumerFactory<MetaDataRegisterDto> {

    @Override
    public SwallowConsumerExecutor<MetaDataRegisterDto> create() {
        return new RegisterClientConsumerExecutor();
    }
}
