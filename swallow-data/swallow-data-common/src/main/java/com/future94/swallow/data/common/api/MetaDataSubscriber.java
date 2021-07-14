package com.future94.swallow.data.common.api;

import com.future94.swallow.common.dto.MetaDataRegisterDto;

/**
 * @author weilai
 */
public interface MetaDataSubscriber {

    void onSubscribe(MetaDataRegisterDto metaData);

    void unSubscribe(MetaDataRegisterDto metaData);

    default void refresh() {
    }
}
