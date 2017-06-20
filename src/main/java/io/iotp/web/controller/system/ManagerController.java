/*
 * Copyright (c) 2015 - 广州小橙信息科技有限公司 
 * All rights reserved.
 *
 * Created on 2017-01-12
 */
package io.iotp.web.controller.system;

import io.iotp.core.system.SystemManagerService;
import io.iotp.member.device.service.UserDeviceTmpService;
import io.springbootstrap.core.api.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 系统管理Controller
 *
 * @author huchiwei
 * @since 1.0.0
 */
@Controller
public class ManagerController {

    @Autowired
    private SystemManagerService systemManagerService;

    @Autowired
    private UserDeviceTmpService userDeviceTmpService;

    @RequestMapping(value = "/cache/{cacheValue}/clear", method = RequestMethod.GET)
    @ResponseBody
    public ResponseData removeAllCache(@PathVariable String cacheValue){
        this.systemManagerService.clearCache(cacheValue);
        return new ResponseData(0, "缓存清理成功");
    }

    @RequestMapping(value = "/test/device/register", method = RequestMethod.GET)
    @ResponseBody
    public ResponseData registerIotTestDevices(){
        this.userDeviceTmpService.registerIotTestDevices();
        return new ResponseData();
    }
 }
