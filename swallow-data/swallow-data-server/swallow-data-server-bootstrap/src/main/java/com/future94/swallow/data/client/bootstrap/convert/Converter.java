package com.future94.swallow.data.client.bootstrap.convert;

import com.future94.swallow.common.dto.MetaDataRegisterDto;
import com.future94.swallow.data.client.bootstrap.entity.MetaData;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author weilai
 */
@Mapper
public interface Converter {

    Converter INSTANCE = Mappers.getMapper(Converter.class);

    MetaDataRegisterDto toDto(MetaData metaData);

    List<MetaDataRegisterDto> toDto(List<MetaData> metaDataList);

    MetaData toEntity(MetaDataRegisterDto registerDto);
}
