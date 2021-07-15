package com.future94.swallow.data.client.bootstrap.service.impl;

import com.future94.swallow.common.dto.MetaDataRegisterDto;
import com.future94.swallow.common.enums.DataEventTypeEnum;
import com.future94.swallow.data.client.bootstrap.convert.Converter;
import com.future94.swallow.data.client.bootstrap.entity.MetaData;
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

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public String save(MetaDataRegisterDto registerDto) {
        try {
            MetaData metaData = metaDataRepository.findByAppNameAndPath(registerDto.getAppName(), registerDto.getPath());
            DataEventTypeEnum eventType = DataEventTypeEnum.UPDATE;
            if (metaData == null) {
                eventType = DataEventTypeEnum.CREATE;
            }
            metaData = Converter.INSTANCE.toEntity(registerDto);
            metaData.setCreateTime(LocalDateTime.now());
            metaData.setUpdateTime(LocalDateTime.now());
            metaDataRepository.save(metaData);
            eventPublisher.publishEvent(new DataChangedEvent(Collections.singletonList(metaData), eventType));
            return "success";
        } catch (Exception e) {
            log.error("update metadata fail, data:{}", new Gson().toJson(registerDto), e);
            return "error";
        }
    }

    @Override
    public List<MetaDataRegisterDto> findAll() {
        return Converter.INSTANCE.toDto(metaDataRepository.findAll());
    }
}
