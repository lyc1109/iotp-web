/**
 * Copyright (c) 2015 - Two Steps From Java Group.
 * All rights reserved.
 *
 * Created on 2017-05-16
 */
package io.iotp.web.config;

import io.iotp.module.iot.service.IotDeviceRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


/**
 * Iot设备启动加载器
 *
 * @author CD826
 * @since 2.0.0
 */
@Component
@Order(value=1)
public class IotDeviceStartupRunner implements CommandLineRunner {
    @Autowired
    private IotDeviceRedisService iotDeviceRedisService;

    @Override
    public void run(String... args) throws Exception {
        this.iotDeviceRedisService.syncToRedis();
    }
}