/**
 * Copyright (c) 2015 - 广州小橙信息科技有限公司
 * All rights reserved.
 *
 * Created on 2016-11-18
 */
package io.iotp.web.controller.shop;

import io.iotp.member.device.dto.UserDeviceDto;
import io.iotp.member.device.service.UserDeviceService;
import io.iotp.module.device.entity.UserDevice;
import io.iotp.module.serviceorder.entity.ServiceOrder;
import io.iotp.module.shop.entity.Shop;
import io.iotp.module.shop.member.entity.ShopMember;
import io.iotp.serviceorder.dto.ServiceOrderDto;
import io.iotp.serviceorder.mapper.ServiceOrderEntityDtoMapper;
import io.iotp.serviceorder.service.ServiceOrderService;
import io.iotp.shop.dto.ShopMemberDto;
import io.iotp.shop.service.ShopMemberService;
import io.iotp.web.controller.IotpCrudController;
import io.springbootstrap.core.annotation.UserLogConfig;
import io.springbootstrap.core.util.IdConfuseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Collections;
import java.util.List;


/**
 * 店铺会员管理Controller
 *
 * @author CD826
 * @since 1.0.0
 */
@Controller
@RequestMapping("/shop/member")
@UserLogConfig(moduleName = "店铺会员管理")
public class ShopMemberController extends IotpCrudController<ShopMember, ShopMemberDto, ShopMemberService> {
    @Autowired
    private ServiceOrderService serviceOrderService;

    @Autowired
    private UserDeviceService userDeviceService;

    @Autowired
    private ServiceOrderEntityDtoMapper serviceOrderEntityDtoMapper;


    // ========================================================================
    // Page/List Methods ======================================================
    @Override
    public Page<ShopMemberDto> getPageData(Pageable pageable, String searchPhrase) {
        Page<ShopMember> page = this.service.getPage(pageable, this.getCurShop().getId(), searchPhrase);
        return this.mapToDtoPage(pageable, page);
    }

    /**
     * 加载会员的服务单列表
     * @return
     */
    @RequestMapping(value = "/{confuseId}/serviceOrder", method = RequestMethod.GET)
    public String serviceOrder(@PathVariable String confuseId){
        long shopMemberId = IdConfuseUtils.decodeId(confuseId);
        ShopMember shopMember = this.service.load(shopMemberId);
        if (null != shopMember) {
            List<ServiceOrder> serviceOrders = this.serviceOrderService.findAll(shopMember.getShopId(), shopMember.getUser().getId(), null);
            this.setAttribute("serviceOrders", this.serviceOrderEntityDtoMapper.mapToDtos(serviceOrders, ServiceOrderDto.class));
        } else {
            this.setAttribute("serviceOrders", Collections.EMPTY_LIST);
        }

        return this.viewPath("/serviceOrderList::listBody");
    }

    /**
     * 加载会员的设备列表
     * @return
     */
    @RequestMapping(value = "/{confuseId}/devices", method = RequestMethod.GET)
    public String devices(@PathVariable String confuseId){
        Shop curShop = this.getCurShop();
        ShopMember shopMember = this.service.load(IdConfuseUtils.decodeId(confuseId));
        List<UserDevice> deviceList = this.userDeviceService.findAll(curShop.getId(), shopMember.getUser().getId());
        this.setAttribute("devices", this.entityDtoMapService.mapToDtos(deviceList, UserDeviceDto.class));
        return this.viewPath("/devicesList::listBody");
    }

    /**
     * 加载会员的评价列表
     * @return
     */
    @RequestMapping(value = "/{confuseId}/rating", method = RequestMethod.GET)
    public String rating(@PathVariable String confuseId){
        long shopMemberId = IdConfuseUtils.decodeId(confuseId);
        ShopMember shopMember = this.service.load(shopMemberId);
        this.setAttribute("ratingLists", Collections.EMPTY_LIST);

        return this.viewPath("/ratingList::listBody");
    }
}
