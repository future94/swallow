package com.future94.swallow.data.client.bootstrap.controller;

import com.future94.swallow.common.dto.MetaDataRegisterDto;
import com.future94.swallow.data.client.bootstrap.service.MetaDataService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author weilai
 */
@RestController
@RequestMapping("/register")
public class MetaDataController {

    private final MetaDataService metaDataService;

    public MetaDataController(MetaDataService metaDataService) {
        this.metaDataService = metaDataService;
    }

    @PostMapping("/metaData")
    public String metaData(@RequestBody MetaDataRegisterDto registerDto) {
        return metaDataService.save(registerDto);
    }
}
