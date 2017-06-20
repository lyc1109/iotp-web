/**
 * Copyright (c) 2015 - 广州小橙信息科技有限公司
 * All rights reserved.
 *
 * Created on 2016-11-21
 */
package io.iotp.web.controller.shopservice;

import io.iotp.module.shopservice.servicedef.entity.ShopServiceItemDef;
import io.iotp.shopservice.dto.ShopServiceItemDefDto;
import io.iotp.shopservice.service.ShopServiceItemDefService;
import io.iotp.web.controller.IotpCrudController;
import io.springbootstrap.core.annotation.UserLogConfig;
import io.springbootstrap.core.annotation.UserLogable;
import io.springbootstrap.core.api.ResponseData;
import io.springbootstrap.core.util.IdConfuseUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * 店铺服务管理Controller
 *
 * @author CD826
 * @since 1.0.0
 */
@Controller
@RequestMapping("/shop/service/item")
@UserLogConfig(moduleName = "服务项目管理")
public class ShopServiceItemController extends IotpCrudController<ShopServiceItemDef, ShopServiceItemDefDto, ShopServiceItemDefService> {
    // ========================================================================
    // Page/List Methods ======================================================
    @Override
    public Page<ShopServiceItemDefDto> getPageData(@PageableDefault(sort = {"orderNo"}, direction = Sort.Direction.DESC) Pageable pageable, String searchPhrase) {
        Page<ShopServiceItemDef> page = this.service.getPage(pageable, this.getCurShop().getId(), this.getCfgStatus(), searchPhrase);
        return this.mapToDtoPage(pageable, page);
    }

    @Override
    protected void onSaveEntity(ShopServiceItemDef entity) {
        super.onSaveEntity(entity);
        entity.setShopId(getCurShop().getId());
    }

    /**
     * 启用服务项目
     * @param confuseId
     * @return
     */
    @RequestMapping(value = "/{confuseId}/enable", method = RequestMethod.POST)
    @ResponseBody
    @UserLogable(methodName = "启用")
    public ResponseData enable(@PathVariable String confuseId){
        this.service.enable(IdConfuseUtils.decodeId(confuseId));
        return new ResponseData();
    }

    /**
     * 禁用服务项目
     * @param confuseId
     * @return
     */
    @RequestMapping(value = "/{confuseId}/disable", method = RequestMethod.POST)
    @ResponseBody
    @UserLogable(methodName = "禁用")
    public ResponseData disable(@PathVariable String confuseId){
        this.service.disable(IdConfuseUtils.decodeId(confuseId));
        return new ResponseData();
    }
}
