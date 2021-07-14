package com.future94.swallow.data.client.bootstrap.controller;

import com.future94.swallow.common.dto.SwallowCommonResponse;
import com.future94.swallow.data.client.bootstrap.listener.HttpLongPollingDataChangedListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author weilai
 */
@RestController
@ConditionalOnBean(HttpLongPollingDataChangedListener.class)
@RequestMapping("/sync")
public class SyncController {

    @Resource
    private HttpLongPollingDataChangedListener httpLongPollingDataChangedListener;

    @PostMapping(value = "/listener")
    public void listener(final HttpServletRequest request, final HttpServletResponse response) {
        httpLongPollingDataChangedListener.doLongPolling(request, response);
    }

    @GetMapping("/fetch")
    public SwallowCommonResponse fetch() {
        return SwallowCommonResponse.success(httpLongPollingDataChangedListener.fetchConfig()) ;
    }
}
