package com.future94.swallow.data.client.bootstrap.service;

import com.future94.swallow.common.dto.MetaDataRegisterDto;

import java.util.List;

/**
 * @author weilai
 */
public interface MetaDataService {
    String save(MetaDataRegisterDto registerDto);

    List<MetaDataRegisterDto> findAll();

    String delete(Integer id);
}
