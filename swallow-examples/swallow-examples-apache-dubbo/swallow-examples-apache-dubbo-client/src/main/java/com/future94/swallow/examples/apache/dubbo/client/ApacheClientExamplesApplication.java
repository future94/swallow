package com.future94.swallow.examples.apache.dubbo.client;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author weilai
 */
@SpringBootApplication
@EnableDubbo
public class ApacheClientExamplesApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApacheClientExamplesApplication.class, args);
    }
}
