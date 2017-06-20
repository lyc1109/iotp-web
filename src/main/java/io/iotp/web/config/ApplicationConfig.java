/**
 * Copyright (c) 2015 - 广州小橙信息科技有限公司
 * All rights reserved.
 *
 * Created on 2016-10-10
 */
package io.iotp.web.config;

import io.iotp.core.security.DefaultWechatProperties;
import io.iotp.core.security.WechatProperties;
import io.springbootstrap.core.security.SmartHttpSessionStrategy;
import io.springbootstrap.wechat.WechatService;
import io.springbootstrap.wechat.WepayService;
import io.springbootstrap.wechat.jssdk.JSSDKAPI;
import io.springbootstrap.wechat.jssdk.JSSDKConfigurator;
import io.springbootstrap.wechat.model.WechatAccount;
import io.springbootstrap.wechat.model.WechatPayAccount;
import io.springbootstrap.wechat.util.TicketType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.HttpSessionStrategy;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.context.support.ServletContextAttributeExporter;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;


/**
 * 应用的基础配置
 *
 * @author CD826
 * @since 1.0.0
 */
@Configuration
@EnableAsync
@EnableCaching
@EnableRedisHttpSession
public class ApplicationConfig {
    @Resource
    private Environment environment;

    /**
     * 统一设置ServletContext参数
     *
     * @return ServletContextAttributeExporter
     */
    @Bean
    public ServletContextAttributeExporter servletContextAttributeExporter() {
        ServletContextAttributeExporter exporter = new ServletContextAttributeExporter();
        Map<String, Object> map = new HashMap<>();

        map.put("isProduct", environment.getProperty("spring.profiles.active").equalsIgnoreCase("product") ||
                environment.getProperty("spring.profiles.active").equalsIgnoreCase("stage"));
        map.put("ossUrl", environment.getProperty("aliyun.oss.access"));

        exporter.setAttributes(map);
        return exporter;
    }

    // ========================================================================
    // HttpSessionStrategy ====================================================
    @Bean
    public HttpSessionStrategy httpSessionStrategy() {
        return new SmartHttpSessionStrategy();
    }

    // ========================================================================
    // wechat config ==========================================================
    @Bean
    public WechatProperties wechatProperties() {
        return new DefaultWechatProperties();
    }

    @Bean
    @Autowired
    public WechatService wechatService(WechatProperties wechatProperties) {
        WechatAccount wechatAccount = new WechatAccount(wechatProperties.getAppId(), wechatProperties.getSecret());
        return new WechatService(wechatAccount);
    }

    @Bean
    @Autowired
    public WepayService wepayService(WechatProperties wechatProperties) {
        return new WepayService(new WechatPayAccount(wechatProperties.getAppId(), wechatProperties.getPaySignKey(), wechatProperties.getMchId()),
                wechatProperties.getTradeNotifyUrl());
    }

    @Bean
    @Autowired
    public JSSDKConfigurator jssdkConfigurator(WechatService wechatService, WechatProperties wechatProperties) {
        JSSDKConfigurator jssdkConfigurator = new JSSDKConfigurator(wechatService.getTicketManager(TicketType.jsapi));
        jssdkConfigurator.appId(wechatProperties.getAppId());
        jssdkConfigurator.apis(JSSDKAPI.SHARE_APIS).apis(JSSDKAPI.SCAN_APIS);
        if (!(environment.getProperty("spring.profiles.active").equalsIgnoreCase("product"))) {
            jssdkConfigurator.debugMode();
        }
        return jssdkConfigurator;
    }
}
