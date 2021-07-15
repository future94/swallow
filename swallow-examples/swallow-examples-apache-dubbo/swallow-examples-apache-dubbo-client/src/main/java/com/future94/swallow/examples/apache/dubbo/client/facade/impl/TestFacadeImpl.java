package com.future94.swallow.examples.apache.dubbo.client.facade.impl;

import com.future94.swallow.client.dubbo.common.annotation.SwallowDubboClient;
import com.future94.swallow.examples.apache.dubbo.client.dto.SuccessParam;
import com.future94.swallow.examples.apache.dubbo.client.facade.TestFacade;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

/**
 * @author weilai
 */
@Service
@DubboService
public class TestFacadeImpl implements TestFacade {

    @Override
    @SwallowDubboClient(path = "/success", pathDesc = "成功无参数")
    public String success() {
        return "success";
    }

    @Override
    @SwallowDubboClient(path = "/success/two", pathDesc = "成功两个参数")
    public String success(String arg1, String arg2) {
        return "success" + arg1 + arg2;
    }

    @Override
    @SwallowDubboClient(path = "/success/body", pathDesc = "成功body参数")
    public String success(SuccessParam param) {
        return param.toString();
    }

    @Override
    @SwallowDubboClient(path = "/error", pathDesc = "失败无参数")
    public String error() {
        throw new RuntimeException("error");
    }
}
