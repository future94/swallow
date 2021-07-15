package com.future94.swallow.examples.alibaba.dubbo.client.facade;

import com.future94.swallow.examples.alibaba.dubbo.client.dto.SuccessParam;

/**
 * @author weilai
 */
public interface TestFacade {

    String success();

    String success(String arg1, String arg2);

    String success(SuccessParam param);

    String error();
}
