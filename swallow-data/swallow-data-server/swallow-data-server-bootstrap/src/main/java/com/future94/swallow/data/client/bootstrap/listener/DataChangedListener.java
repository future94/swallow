package com.future94.swallow.data.client.bootstrap.listener;

import com.future94.swallow.common.dto.MetaDataRegisterDto;
import com.future94.swallow.common.enums.DataEventTypeEnum;
import com.future94.swallow.data.client.bootstrap.entity.MetaData;

import java.util.List;

/**
 * @author weilai
 */
public interface DataChangedListener {

    void onMetaDataChanged(List<MetaDataRegisterDto> changed, DataEventTypeEnum eventType);
}
