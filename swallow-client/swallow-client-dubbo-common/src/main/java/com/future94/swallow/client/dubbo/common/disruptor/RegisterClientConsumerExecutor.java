package com.future94.swallow.client.dubbo.common.disruptor;

import com.future94.swallow.common.disruptor.consumer.SwallowConsumerExecutor;
import com.future94.swallow.common.dto.MetaDataRegisterDto;
import com.future94.swallow.common.dto.ServerConfig;
import com.future94.swallow.common.utils.OkHttpUtils;
import com.future94.swallow.common.constants.StatusConstants;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

/**
 * @author weilai
 */
@Slf4j
public class RegisterClientConsumerExecutor extends SwallowConsumerExecutor<MetaDataRegisterDto> {

    private final Gson gson = new Gson();

    private static final String REGISTER_URL = "/register/metaData/save";

    private List<String> serverList;

    @Override
    public void init(ServerConfig serverConfig) {
        this.serverList = serverConfig.getServerList();
    }

    @Override
    public void run() {
        MetaDataRegisterDto registerDto = getData();
        for (String server : serverList) {
            String url = server + REGISTER_URL;
            try {
                String metadata = gson.toJson(registerDto);
                String result = OkHttpUtils.getInstance().post(url, metadata);
                if (StatusConstants.SUCCESS.equals(result)) {
                    log.info("register dubbo service success, serverAdder:[{}], url:[{}], metadata:{}", server, registerDto.getPath(), metadata);
                } else {
                    log.error("register dubbo service error, serverAdder:[{}], url:[{}], metadata:{}", server, registerDto.getPath(), metadata);
                }
            } catch (IOException e) {
                log.error("register dubbo service error, url:[{}]", url, e);
            }
        }
    }
}
