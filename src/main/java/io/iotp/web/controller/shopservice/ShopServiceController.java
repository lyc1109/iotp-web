/**
 * Copyright (c) 2015 - 广州小橙信息科技有限公司
 * All rights reserved.
 *
 * Created on 2016-11-21
 */
package io.iotp.web.controller.shopservice;

import io.iotp.module.shopservice.servicedef.entity.ShopServiceDef;
import io.iotp.shopservice.dto.ShopServiceDefDto;
import io.iotp.shopservice.service.ShopServiceDefService;
import io.iotp.web.controller.IotpCrudController;
import io.springbootstrap.core.annotation.UserLogConfig;
import io.springbootstrap.core.annotation.UserLogable;
import io.springbootstrap.core.api.ResponseData;
import io.springbootstrap.core.util.IdConfuseUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 店铺服务管理Controller
 *
 * @author CD826
 * @since 1.0.0
 */
@Controller
@RequestMapping("/shop/service")
@UserLogConfig(moduleName = "服务类目管理")
public class ShopServiceController extends IotpCrudController<ShopServiceDef, ShopServiceDefDto, ShopServiceDefService> {
    // ========================================================================
    // Page/List Methods ======================================================
    @Override
    protected void onList() {
        this.forwardParameters("searchPhrase");
    }

    @Override
    public Page<ShopServiceDefDto> getPageData(Pageable pageable, String searchPhrase) {
        Page<ShopServiceDef> page = this.service.getPage(pageable, this.getCurShop().getId(), this.getCfgStatus(), searchPhrase);
        return this.mapToDtoPage(pageable, page);
    }

    @Override
    protected void onSaveEntity(ShopServiceDef entity) {
        super.onSaveEntity(entity);
        entity.setShopId(getCurShop().getId());
    }

    /**
     * 启用服务类目
     * @param confuseId 服务id
     * @return ResponseData处理结果
     */
    @RequestMapping(value = "/{confuseId}/enable", method = RequestMethod.POST)
    @ResponseBody
    @UserLogable(methodName = "启用")
    public ResponseData enable(@PathVariable String confuseId){
        this.service.enable(IdConfuseUtils.decodeId(confuseId));
        return new ResponseData();
    }

    /**
     * 禁用服务类目
     * @param confuseId  服务id
     * @return ResponseData处理结果
     */
    @RequestMapping(value = "/{confuseId}/disable", method = RequestMethod.POST)
    @ResponseBody
    @UserLogable(methodName = "禁用")
    public ResponseData disable(@PathVariable String confuseId){
        this.service.disable(IdConfuseUtils.decodeId(confuseId));
        return new ResponseData();
    }

    /**
     * 弹出对话框选择服务类目
     *
     * @return 选择服务类目对话框路径
     */
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public String select(Model model, @RequestParam(defaultValue = "") String selectedServiceIds){
        List<ShopServiceDef> shopServices = this.service.findByShopId(getCurShop().getId());
        model.addAttribute("shopServices", this.mapToDtos(shopServices));
        model.addAttribute("selectedServiceIds", selectedServiceIds);
        return this.viewPath("/selectDlg::content");
    }
}
