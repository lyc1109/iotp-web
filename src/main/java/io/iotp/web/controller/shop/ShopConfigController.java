/**
 * Copyright (c) 2015 - 广州小橙信息科技有限公司
 * All rights reserved.
 *
 * Created on 2016-11-21
 */
package io.iotp.web.controller.shop;

import io.iotp.core.security.IotpSessionContext;
import io.iotp.shop.dto.ShopDto;
import io.iotp.shop.service.ShopService;
import io.iotp.module.shop.entity.Shop;
import io.iotp.web.controller.IotpCrudController;
import io.springbootstrap.core.annotation.UserLogConfig;
import io.springbootstrap.core.annotation.UserLogable;
import io.springbootstrap.core.api.ResponseData;
import io.springbootstrap.core.util.ApplicationContextHolder;
import io.springbootstrap.core.util.IdConfuseUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 店铺基本配置管理Controller
 *
 * @author CD826
 * @since 1.0.0
 */
@Controller
@RequestMapping("/shop/config")
@UserLogConfig(moduleName = "店铺配置管理")
public class ShopConfigController extends IotpCrudController<Shop, ShopDto, ShopService> {

    @Override
    public Page<ShopDto> getPageData(Pageable pageable, String searchPhrase) {
        return null;
    }

    /**
     * 基本配置页面
     *
     * @return 页面路径
     */
    @RequestMapping(value = {"", "basic"}, method = RequestMethod.GET)
    public String basicCfg(Model model){
        Shop curShop = getCurShop();
        if(null != curShop){
            curShop = this.service.load(curShop.getId());
            model.addAttribute(ENTITY_NAME, this.entityDtoMapService.mapToDto(curShop, ShopDto.class));
        }
        return this.viewPath("/basic");
    }

    /**
     * 微信配置页面
     * @return
     */
    @RequestMapping(value = "wechat", method = RequestMethod.GET)
    @UserLogable(methodName = "微信配置")
    public String wechatCfg(Model model){
        String shopUrl = ApplicationContextHolder.getEnvProperty("app.server.url");
        shopUrl += "/shop_" + IdConfuseUtils.encodeId(IotpSessionContext.getShop().getId());
        model.addAttribute("shopUrl", shopUrl);
        return this.viewPath("/wechat");
    }

    /**
     * 更新店铺信息
     * @return
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    @UserLogable(methodName = "更新")
    public ResponseData update(){
        Shop shop = this.service.load(getCurShop().getId());
        shop.setName(this.getParameter("name", ""));
        shop.setContactName(this.getParameter("contactName", ""));
        shop.setContactMobile(this.getParameter("contactMobile", ""));
        shop.setContactWechat(this.getParameter("contactWechat", ""));
        shop.setContactQq(this.getParameter("contactQq", ""));
        shop.setProvince(this.getParameter("province", ""));
        shop.setCity(this.getParameter("city", ""));
        shop.setArea(this.getParameter("area", ""));
        shop.setAddress(this.getParameter("address", ""));
        shop.setShopProfile(this.getParameter("shopProfile", ""));
        this.service.save(shop);
        return new ResponseData();
    }

    /**
     * 更新店铺Logo
     * @return
     */
    @RequestMapping(value = "/updateLogo", method = RequestMethod.POST)
    @ResponseBody
    @UserLogable(methodName = "修改Logo")
    public ResponseData updateLogo(){
        Shop shop = this.service.load(getCurShop().getId());
        shop.setLogoImageId(IdConfuseUtils.decodeId(this.getParameter("logoImageId", "")));
        this.service.save(shop);
        return new ResponseData();
    }
}
