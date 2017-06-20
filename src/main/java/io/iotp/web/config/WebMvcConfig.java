/**
 * Copyright (c) 2015 - 广州小橙信息科技有限公司
 * All rights reserved.
 *
 * Created on 2016-10-28
 */
package io.iotp.web.config;

import io.springbootstrap.core.support.BootGridPageableHandlerMethodArgumentResolver;
import io.springbootstrap.core.support.OptionalPathVariableMethodArgumentResolver;
import io.springbootstrap.core.support.RequestJsonParamMethodArgumentResolver;
import io.springbootstrap.core.userlog.UserLogAdapter;
import io.springbootstrap.core.userlog.UserLogHandlerInterceptorAdapter;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.Resource;
import java.util.List;

/**
 * Spring MVC配置
 *
 * @author CD826
 * @since 1.0.0
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {
    @Resource
    private UserLogAdapter userLogAdapter;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new BootGridPageableHandlerMethodArgumentResolver());
        argumentResolvers.add(new OptionalPathVariableMethodArgumentResolver());
        argumentResolvers.add(new RequestJsonParamMethodArgumentResolver());

        super.addArgumentResolvers(argumentResolvers);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserLogHandlerInterceptorAdapter(this.userLogAdapter));

        super.addInterceptors(registry);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**");
    }
}
