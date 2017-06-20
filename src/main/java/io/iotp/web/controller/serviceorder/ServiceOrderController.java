/**
 * Copyright (c) 2015 - 广州小橙信息科技有限公司
 * All rights reserved.
 *
 * Created on 2016-10-10
 */
package io.iotp.web.controller.serviceorder;

import com.xiaochenghudong.core.Address;
import io.iotp.core.security.IotpSessionContext;
import io.iotp.module.rating.entity.Rating;
import io.iotp.module.rating.entity.RatingReply;
import io.iotp.module.serviceorder.ServiceOrderForm;
import io.iotp.module.serviceorder.entity.ServiceOrder;
import io.iotp.module.shop.entity.Shop;
import io.iotp.module.shop.entity.ShopEmployee;
import io.iotp.module.shopservice.servicedef.entity.ShopServiceDef;
import io.iotp.rating.dto.RatingReplyDto;
import io.iotp.rating.mapper.RatingReplyEntityDtoMapper;
import io.iotp.rating.service.RatingService;
import io.iotp.serviceorder.dto.ServiceOrderDto;
import io.iotp.serviceorder.service.ServiceOrderService;
import io.iotp.shop.service.ShopEmployeeService;
import io.iotp.shopservice.dto.ShopServiceDefDto;
import io.iotp.shopservice.service.ShopServiceDefService;
import io.iotp.web.controller.IotpCrudController;
import io.springbootstrap.core.annotation.UserLogConfig;
import io.springbootstrap.core.annotation.UserLogable;
import io.springbootstrap.core.api.ResponseData;
import io.springbootstrap.core.exception.ServiceException;
import io.springbootstrap.core.util.DateUtils;
import io.springbootstrap.core.util.IdConfuseUtils;
import io.springbootstrap.core.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 服务订单管理Controller
 *
 * @author CD826
 * @since 1.0.0
 */
@Controller
@RequestMapping("/shop/serviceOrder")
@UserLogConfig(moduleName = "服务订单管理")
public class ServiceOrderController extends IotpCrudController<ServiceOrder, ServiceOrderDto, ServiceOrderService> {
    // ========================================================================
    // constants ==============================================================
    public static final int VT_DFP                  = 10;       // 视图类型：待分派
    public static final int VT_JXZ                  = 20;       // 视图类型：进行中
    public static final int VT_DFK                  = 30;       // 视图类型：待付款
    public static final int VT_YWC                  = 40;       // 视图类型：已完成
    public static final int VT_TKZ                  = 50;       // 视图类型：退款中
    public static final int VT_DQX                  = 85;       // 视图类型：待取消
    public static final int VT_QB                   = 99;       // 视图类型：全部

    @Autowired
    private ShopServiceDefService shopServiceDefService;

    @Autowired
    private ShopEmployeeService shopEmployeeService;

    @Autowired
    private RatingService ratingService;

    @Autowired
    RatingReplyEntityDtoMapper ratingReplyEntityDtoMapper;

    // ========================================================================
    // Page/List Methods ======================================================
    /**
     * 列表页面
     * @return
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    @UserLogable(methodName = "视图列表")
    public String list(){
        this.setAttribute("viewType",  this.getIntParameter("viewType", VT_DFP));
        this.forwardParameters("shopService", "serviceDate");

        // 加载服务类目选项
        long shopId = IotpSessionContext.getShop().getId();
        this.setAttribute("serviceOptions", this.shopServiceDefService.getServiceDefOptions(shopId));

        return this.listPagePath();
    }

    @Override
    public Page<ServiceOrderDto> getPageData(Pageable pageable, String searchPhrase) {
        Shop shop = IotpSessionContext.getShop();
        if(null == shop) {
            return null;
        }

        // 组合查询条件
        Map<String, Object> searchFields = new HashMap<>();

        // 订单类型
        String serviceName = this.getParameter("shopService", "");
        if (serviceName.equals(ServiceOrder.OT_LEASE)) {
            // 租赁预约订单
            searchFields.put("orderType", serviceName);
        } else {
            // 服务订单
            long shopService = IdConfuseUtils.decodeId(serviceName);
            if (shopService > 0) {
                searchFields.put("shopServiceId", shopService);
            }
        }

        // 预约日期
        int serviceDate = this.getIntParameter("serviceDate", 0);
        if (serviceDate > 0) {
            searchFields.put("serviceDate", serviceDate);
        }

        int viewType = this.getIntParameter("viewType", VT_DFP);
        int[] status = null;
        if(VT_DFP == viewType) {
            status = new int[]{ServiceOrder.SOT_TOBECONFIRMED, ServiceOrder.SOT_TOBEASSIGNE, ServiceOrder.SOT_REDISPATCH};
        } else if(VT_JXZ == viewType) {
            status = new int[]{ServiceOrder.SOT_ASSIGNED, ServiceOrder.SOT_PROCESSING_UNPAY, ServiceOrder.SOT_PROCESSING_PAIED};
        } else if(VT_DFK == viewType) {
            status = new int[]{ServiceOrder.SOT_TOBEPAID, ServiceOrder.SOT_TOBEPAYCONFIRMED};
        } else if(VT_YWC == viewType) {
            status = new int[]{ServiceOrder.SOT_TOBERATING, ServiceOrder.SOT_CLOSED, ServiceOrder.SOT_CANCELED, ServiceOrder.SOT_REFUND, ServiceOrder.SOT_REJECTED};
        } else if(VT_TKZ == viewType) {
            status = new int[]{ServiceOrder.SOT_REFUND};
        } else if(VT_DQX == viewType) {
            status = new int[]{ServiceOrder.SOT_CANCELED_CONFIRM};
        }

        return this.mapToDtoPage(pageable, this.service.getPage(pageable, shop.getId(), status, searchFields, searchPhrase));
    }

    @Override
    protected void onOpenCreateForm(Model model, ServiceOrderDto dto) {
        super.onOpenCreateForm(model, dto);

        // 加载服务类目
        List<ShopServiceDef> serviceDefs = this.shopServiceDefService.findByShopId(getCurShop().getId());
        model.addAttribute("serviceDefs", this.entityDtoMapService.mapToDtos(serviceDefs, ShopServiceDefDto.class));

        //
    }

    @Override
    protected void onOpenViewForm(Model model, ServiceOrderDto dto) {
        super.onOpenViewForm(model, dto);
        Rating rating = null;
        if(Shop.ST_MANUFACTURER.equals(this.getCurShop().getShopType())){
            //厂商
            rating = this.ratingService.findByTradeOrderId(IdConfuseUtils.decodeId(dto.getId()),this.getCurShop().getId(),0);
        }else{
            //服务商
            rating = this.ratingService.findByTradeOrderId(IdConfuseUtils.decodeId(dto.getId()),0,this.getCurShop().getId());
        }

        if( rating != null){
            List<RatingReply> ratingReplies = rating.getRatingReplies();
            if(ratingReplies.size()>0){
                model.addAttribute("ratingReplies",ratingReplyEntityDtoMapper.mapToDtos(ratingReplies, RatingReplyDto.class));
            }
        }
    }

    /**
     * 将服务服务订单分派给指定的服务商
     * @return
     */
    @RequestMapping(value = "/{serviceOrderId}/assignToVendor", method = RequestMethod.POST)
    @ResponseBody
    @UserLogable(methodName = "派单")
    public ResponseData assignToVendor(@PathVariable String serviceOrderId){
        long orderId = IdConfuseUtils.decodeId(serviceOrderId);
        long serviceVendorId = IdConfuseUtils.decodeId(this.getParameter("serviceVendorId", "-1"));

        ResponseData responseData = new ResponseData();
        try {
            this.service.assignToVendor(IotpSessionContext.getShopEmployee(), orderId, serviceVendorId);
        } catch (ServiceException se) {
            responseData.setReturnCode(ResponseData.ERROR_RETURN_CODE);
            responseData.setReturnMsg(se.getMessage());
        }
        return responseData;
    }

    /**
     * 厂商拒绝受理服务订单
     * @return
     */
    @RequestMapping(value = "/{serviceOrderId}/reject", method = RequestMethod.POST)
    @ResponseBody
    @UserLogable(methodName = "拒绝")
    public ResponseData reject(@PathVariable String serviceOrderId){
        long orderId = IdConfuseUtils.decodeId(serviceOrderId);
        String rejectReason = this.getParameter("rejectReason", "");
        ResponseData responseData = new ResponseData();
        try {
            this.service.reject(IotpSessionContext.getShopEmployee(), orderId, rejectReason);
        } catch (ServiceException se) {
            responseData.setReturnCode(ResponseData.ERROR_RETURN_CODE);
            responseData.setReturnMsg(se.getMessage());
        }
        return responseData;
    }

    /**
     * 重新派单
     *
     * @return 派单结果
     */
    @RequestMapping(value = "/{serviceOrderId}/reassign", method = RequestMethod.POST)
    @ResponseBody
    @UserLogable(methodName = "重新派单", description = "'服务订单ID：' + #serviceOrderId ")
    public ResponseData reassign(@PathVariable String serviceOrderId){
        long orderId = IdConfuseUtils.decodeId(serviceOrderId);
        long serviceVendorId = IdConfuseUtils.decodeId(this.getParameter("serviceVendorId", "-1"));

        ResponseData responseData = new ResponseData();
        try {
            this.service.reAssignToVendor(IotpSessionContext.getShopEmployee(), orderId, serviceVendorId);
        } catch (ServiceException se) {
            responseData.setReturnCode(ResponseData.ERROR_RETURN_CODE);
            responseData.setReturnMsg(se.getMessage());
        }
        return responseData;
    }

    /**
     * 厂商新增订单
     *
     * @return 新增订单结果
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST, headers="X-Requested-With")
    @ResponseBody
    @UserLogable(methodName = "厂商新增订单", description = "'订单类型：' + #serviceOrderForm.orderType")
    public ResponseData submitServiceRequest(@ModelAttribute ServiceOrderForm serviceOrderForm){
        // 日期处理
        String serviceDateTime = this.getParameter("serviceDateTime", "");
        if(StringUtils.hasText(serviceDateTime))
            serviceOrderForm.setServiceDate(DateUtils.parseDate(serviceDateTime));

        // 设备购买时间
        String devicePurchasedDate = this.getParameter("devicePurchasedDate", "");
        if(StringUtils.hasText(devicePurchasedDate))
            serviceOrderForm.setPurchasedDate(DateUtils.parseDate(devicePurchasedDate));

        // 店铺服务
        String shopServiceConfuseId = this.getParameter("shopServiceConfuseId", "");
        if(StringUtils.hasText(shopServiceConfuseId)){
            serviceOrderForm.setShopServiceId(IdConfuseUtils.decodeId(shopServiceConfuseId));
        }

        // 已购买设备id
        String deviceConfuseId = this.getParameter("deviceConfuseId", "");
        if(StringUtils.hasText(deviceConfuseId)){
            serviceOrderForm.setDeviceId(IdConfuseUtils.decodeId(deviceConfuseId));
        }else{
            serviceOrderForm.setDeviceId(-1L);
        }

        // 产品信息
        String productConfuseId = this.getParameter("productConfuseId", "");
        if(StringUtils.hasText(productConfuseId)){
            serviceOrderForm.setProductId(IdConfuseUtils.decodeId(productConfuseId));
        }else{
            serviceOrderForm.setProductId(-1L);
        }

        // 产品规格信息
        String productSpecConfuseId = this.getParameter("productSpecConfuseId", "");
        if(StringUtils.hasText(productSpecConfuseId)){
            serviceOrderForm.setProductSpecId(IdConfuseUtils.decodeId(productSpecConfuseId));
        }

        // 所属租赁产品id
        String leaseProductConfuseId = this.getParameter("leaseProductConfuseId", "");
        if(StringUtils.hasText(leaseProductConfuseId)){
            serviceOrderForm.setLeaseProductId(IdConfuseUtils.decodeId(leaseProductConfuseId));
            serviceOrderForm.setPurchasedDate(null);
        }

        // 地址信息
        String province = this.getParameter("province", "");
        String city = this.getParameter("city", "");
        String area = this.getParameter("area", "");
        String address = this.getParameter("address", "");
        Address serviceAddress = new Address(province, city, area, address);
        serviceOrderForm.setServiceAddress(serviceAddress);

        ShopEmployee curEmployee = this.shopEmployeeService.find(getCurShop().getId(), getCurUser().getId());
        if(null == curEmployee)
            return new ResponseData(ResponseData.ERROR_RETURN_CODE, "无法加载当前员工信息，订单创建失败");

        ServiceOrder serviceOrder = this.service.create(curEmployee, serviceOrderForm);

        ResponseData responseData = new ResponseData();
        responseData.put("orderId", serviceOrder.getConfuseId());
        return responseData;
    }
}
