package com.future94.swallow.common.disruptor.event;

import lombok.Data;

/**
 * @author weilai
 */
@Data
public class SwallowEvent<T> {

    private T data;
}
