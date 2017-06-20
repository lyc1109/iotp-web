/**
 * Copyright (c) 2015 - 广州小橙信息科技有限公司
 * All rights reserved.
 *
 * Created on 2016-10-10
 */
package io.iotp.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;


/**
 * SpringBootstrap Web应用
 *
 * @author CD826
 * @since 1.0.0
 */
@Configuration
@EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class})
@ImportResource("classpath:applicationContext.xml")
@ComponentScan("io.iotp.**,com.xiaochenghudong.**,io.springbootstrap.**,org.twostepsfromjava.**")
public class Application extends SpringBootServletInitializer {
    public static void main(String[] args) {
        new SpringApplication(Application.class).run(args);
    }
}
