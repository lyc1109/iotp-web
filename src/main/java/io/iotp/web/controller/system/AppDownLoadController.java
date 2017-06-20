/*
 * Copyright (c) 2016 - 广东小哈科技股份有限公司 
 * All rights reserved.
 *
 * Created on 2017-04-11
 */
package io.iotp.web.controller.system;

import io.springbootstrap.core.controller.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * AppDownLoadController
 *
 * @author huchiwei
 * @since 2.0.0
 */
@Controller
@RequestMapping("/download/apk")
public class AppDownLoadController extends AbstractController{

    @RequestMapping(value = "/smartlink", method = RequestMethod.GET)
    public String downSmartLink(){
        return this.viewPath("/smartlink");
    }

    @RequestMapping(value = "/smartlink/factory", method = RequestMethod.GET)
    public String downSmartLinkFactory(){
        return this.viewPath("/smartlink-factory");
    }
}
