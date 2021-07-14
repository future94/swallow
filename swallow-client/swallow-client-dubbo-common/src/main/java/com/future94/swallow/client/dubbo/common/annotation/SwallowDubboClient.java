package com.future94.swallow.client.dubbo.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author weilai
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
public @interface SwallowDubboClient {

    /**
     * 路径信息
     * @return string
     */
    String path();

    /**
     * 路径描述
     * @return string
     */
    String pathDesc() default "";

    /**
     * 是否注册
     * @return boolean
     */
    boolean enabled() default true;
}
