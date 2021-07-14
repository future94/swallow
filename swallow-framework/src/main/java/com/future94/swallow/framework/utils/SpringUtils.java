package com.future94.swallow.framework.utils;

import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author weilai
 */
public class SpringUtils {

    private static final SpringUtils INSTANCE = new SpringUtils();

    private ConfigurableApplicationContext cfgContext;

    private SpringUtils() {
    }

    /**
     * get SpringBeanUtils.
     *
     * @return SpringBeanUtils
     */
    public static SpringUtils getInstance() {
        return INSTANCE;
    }

    /**
     * acquire spring bean.
     *
     * @param type type
     * @param <T>  class
     * @return bean
     */
    public <T> T getBean(final Class<T> type) {
        return cfgContext.getBean(type);
    }

    /**
     * set application context.
     *
     * @param cfgContext application context
     */
    public void setCfgContext(final ConfigurableApplicationContext cfgContext) {
        this.cfgContext = cfgContext;
    }
}
