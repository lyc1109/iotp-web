/**
 * Copyright (c) 2015 - 广州小橙信息科技有限公司
 * All rights reserved.
 *
 * Created on 2016-11-21
 */
package io.iotp.web.controller.shopservice;

import io.iotp.shop.service.ShopService;
import io.springbootstrap.core.controller.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 店铺服务时间配置管理Controller
 *
 * @author CD826
 * @since 1.0.0
 */
@Controller
@RequestMapping("/shop/service/time")
public class ShopServiceTimeController extends AbstractController {
    @Autowired
    private ShopService shopService;

    /**
     * 列表页面
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public String timeCfg(){
        return this.viewPath("/config");
    }
}
