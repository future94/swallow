package com.future94.swallow.spring.boot.common.configuration;

import com.future94.swallow.framework.utils.SpringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * @author weilai
 */
@Component
public class SwallowApplicationContextAware implements ApplicationContextAware {

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        SpringUtils.getInstance().setCfgContext((ConfigurableApplicationContext) applicationContext);
    }
}
