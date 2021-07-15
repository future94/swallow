package com.future94.swallow.examples.alibaba.dubbo.client;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author weilai
 */
@SpringBootApplication
@EnableDubbo
public class AlibabaClientExamplesApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlibabaClientExamplesApplication.class, args);
    }
}
