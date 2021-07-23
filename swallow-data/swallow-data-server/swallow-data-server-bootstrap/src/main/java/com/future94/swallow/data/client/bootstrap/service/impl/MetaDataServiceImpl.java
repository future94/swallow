package com.future94.swallow.data.client.bootstrap.service.impl;

import com.future94.swallow.common.constants.StatusConstants;
import com.future94.swallow.common.dto.MetaDataRegisterDto;
import com.future94.swallow.common.enums.DataEventTypeEnum;
import com.future94.swallow.data.client.bootstrap.convert.Converter;
import com.future94.swallow.data.client.bootstrap.entity.MetaData;
import com.future94.swallow.data.client.bootstrap.listener.DataChangeEventMulticaster;
import com.future94.swallow.data.client.bootstrap.listener.DataChangedEvent;
import com.future94.swallow.data.client.bootstrap.repo.MetaDataRepository;
import com.future94.swallow.data.client.bootstrap.service.MetaDataService;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * @author weilai
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MetaDataServiceImpl implements MetaDataService {

    private final MetaDataRepository metaDataRepository;

    private final DataChangeEventMulticaster eventPublisher;

    @Override
    public String save(MetaDataRegisterDto registerDto) {
        try {
            MetaData metaData = metaDataRepository.findByAppNameAndPath(registerDto.getAppName(), registerDto.getPath());
            MetaData record = Converter.INSTANCE.toEntity(registerDto);
            DataEventTypeEnum eventType = DataEventTypeEnum.UPDATE;
            if (metaData == null) {
                eventType = DataEventTypeEnum.CREATE;
            } else {
                record.setId(metaData.getId());
            }
            record.setCreateTime(LocalDateTime.now());
            record.setUpdateTime(LocalDateTime.now());
            metaDataRepository.save(record);
            eventPublisher.publishEvent(record, eventType);
            return StatusConstants.SUCCESS;
        } catch (Exception e) {
            log.error("update metadata error, data:{}", new Gson().toJson(registerDto), e);
            return StatusConstants.ERROR;
        }
    }

    @Override
    public List<MetaDataRegisterDto> findAll() {
        return Converter.INSTANCE.toDto(metaDataRepository.findAll());
    }

    @Override
    public String delete(Integer id) {
        try {
            metaDataRepository.findById(id).ifPresent(metaData -> {
                metaDataRepository.delete(metaData);
                eventPublisher.publishEvent(metaData, DataEventTypeEnum.DELETE);
            });
            return StatusConstants.SUCCESS;
        } catch (Exception e) {
            log.error("delete metadata error, metadataId:{}", id, e);
            return StatusConstants.ERROR;
        }
    }
}
