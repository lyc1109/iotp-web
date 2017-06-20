/**
 * Copyright (c) 2015 - 广州小橙信息科技有限公司
 * All rights reserved.
 *
 * Created on 2016-11-03
 */
package io.iotp.web.controller.workorder;

import io.iotp.core.security.IotpSessionContext;
import io.iotp.module.rating.entity.Rating;
import io.iotp.module.rating.entity.RatingReply;
import io.iotp.module.serviceorder.entity.ServiceOrder;
import io.iotp.module.shop.entity.Shop;
import io.iotp.module.workorder.entity.WorkOrder;
import io.iotp.rating.dto.RatingReplyDto;
import io.iotp.rating.mapper.RatingReplyEntityDtoMapper;
import io.iotp.rating.service.RatingService;
import io.iotp.shopservice.service.ShopServiceDefService;
import io.iotp.web.controller.IotpCrudController;
import io.iotp.workorder.dto.WorkOrderDto;
import io.iotp.workorder.service.WorkOrderService;
import io.springbootstrap.core.annotation.UserLogConfig;
import io.springbootstrap.core.annotation.UserLogable;
import io.springbootstrap.core.api.ResponseData;
import io.springbootstrap.core.exception.ServiceException;
import io.springbootstrap.core.util.IdConfuseUtils;
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
 * 服务工单管理Controller
 *
 * @author CD826
 * @since 1.0.0
 */
@Controller
@RequestMapping("/shop/workOrder")
@UserLogConfig(moduleName = "服务工单管理")
public class WorkOrderController extends IotpCrudController<WorkOrder, WorkOrderDto, WorkOrderService> {
    // ========================================================================
    // constants ==============================================================
    public static final int VT_DPD                  = 10;       // 视图类型：待派单
    public static final int VT_JXZ                  = 20;       // 视图类型：进行中
    public static final int VT_DFK                  = 30;       // 视图类型：待付款
    public static final int VT_YWC                  = 40;       // 视图类型：已完成
    public static final int VT_TKZ                  = 50;       // 视图类型：退款中
    public static final int VT_DQX                  = 85;       // 视图类型：待取消
    public static final int VT_QB                   = 99;       // 视图类型：全部

    @Autowired
    private ShopServiceDefService shopServiceDefService;

    @Autowired
    private RatingService ratingService;

    @Autowired
    RatingReplyEntityDtoMapper ratingReplyEntityDtoMapper;

    // ========================================================================
    // Page/List Methods ======================================================
    @Override
    protected void onList() {
        super.onList();

        this.setAttribute("viewType",  this.getIntParameter("viewType", VT_DPD));
        this.forwardParameters("shopService", "serviceDate");

        // 判断是否是服务商
        Shop shop = IotpSessionContext.getShop();
        if (Shop.ST_SERVICE_VENDOR.equalsIgnoreCase(shop.getShopType())) {
            this.setAttribute("isServiceVendor", true);
            // 加载服务类目选项
            this.setAttribute("serviceOptions", this.service.findShopServiceOptions(shop.getId(), null));
        } else {
            this.setAttribute("isServiceVendor", false);

            // 加载服务类目选项
            this.setAttribute("serviceOptions", this.shopServiceDefService.getServiceDefOptions(shop.getId()));
        }

    }

    @Override
    public Page<WorkOrderDto> getPageData(Pageable pageable, String searchPhrase) {
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

        int viewType = this.getIntParameter("viewType", VT_DPD);
        int[] status = null;
        if(VT_DPD == viewType) {
            status = new int[]{WorkOrder.WOT_TOBECONFIRMED, WorkOrder.WOT_REDISPATCH, WorkOrder.WOT_TOBEASSIGNE};
        } else if(VT_JXZ == viewType) {
            status = new int[]{WorkOrder.WOT_TOBEACCEPT, WorkOrder.WOT_ASSIGNED, WorkOrder.WOT_PROCESSING_UNPAY, WorkOrder.WOT_PROCESSING_PAID};
        } else if(VT_DFK == viewType) {
            status = new int[]{WorkOrder.WOT_TOBEPAID};
        } else if(VT_YWC == viewType) {
            status = new int[]{WorkOrder.WOT_TOBERATING, WorkOrder.WOT_CLOSED, WorkOrder.WOT_CANCELED, WorkOrder.WOT_REFUND};
        } else if(VT_TKZ == viewType) {
            status = new int[]{WorkOrder.WOT_REFUND};
        }  else if(VT_DQX == viewType) {
            status = new int[]{WorkOrder.WOT_CANCELED_CONFIRM};
        } else {
            // null
        }

        Page<WorkOrder> page;
        if (Shop.ST_SERVICE_VENDOR.equalsIgnoreCase(shop.getShopType())) {
            // 店铺是服务商, 那么根据服务商来获取
            page = this.service.getPageByServiceVendor(pageable, shop.getId(), status, searchFields, searchPhrase);
        } else {
            page = this.service.getPageByShop(pageable, shop.getId(), status, searchFields, searchPhrase);
        }
        return this.mapToDtoPage(pageable, page);
    }

    @Override
    protected void onOpenViewForm(Model model, WorkOrderDto dto) {
        super.onOpenViewForm(model, dto);

        // 判断是否是服务商
        Shop shop = IotpSessionContext.getShop();
        if(null != shop)
            model.addAttribute("isServiceOrder", shop.getConfuseId().equalsIgnoreCase(dto.getServiceVendorId()));
        //评价回复
        Rating rating = null;
        if(Shop.ST_MANUFACTURER.equals(this.getCurShop().getShopType())){
            //厂商
            rating = this.ratingService.findByWorkOrderId(IdConfuseUtils.decodeId(dto.getId()),this.getCurShop().getId(),0);
        }else{
            //服务商
            rating = this.ratingService.findByWorkOrderId(IdConfuseUtils.decodeId(dto.getId()),0,this.getCurShop().getId());
        }

        if( rating != null){
            List<RatingReply> ratingReplies = rating.getRatingReplies();
            if(ratingReplies.size()>0){
                model.addAttribute("ratingReplies",ratingReplyEntityDtoMapper.mapToDtos(ratingReplies, RatingReplyDto.class));
            }
        }

    }

    /**
     * 将服务工单分派指定的用户
     *
     * @param workOrderId  工单id
     * @return 分派结果ResponseData
     */
    @RequestMapping(value = "/{workOrderId}/assign", method = RequestMethod.POST)
    @ResponseBody
    @UserLogable(methodName = "派单")
    public ResponseData assign(@PathVariable String workOrderId){
        long id = IdConfuseUtils.decodeId(workOrderId);
        long assigneeId = IdConfuseUtils.decodeId(this.getParameter("assigneeId", "-1"));

        ResponseData responseData = new ResponseData();
        try {
            this.service.assign(IotpSessionContext.getShopEmployee(), id, assigneeId);
        } catch (ServiceException se) {
            responseData.setReturnCode(ResponseData.ERROR_RETURN_CODE);
            responseData.setReturnMsg(se.getMessage());
        }
        return responseData;
    }

    /**
     * 拒绝工单
     *
     * @param workOrderId  工单id
     * @param rejectReason 拒绝理由
     * @return 拒绝结果ResponseData
     */
    @RequestMapping(value = "/{workOrderId}/reject", method = RequestMethod.POST)
    @ResponseBody
    @UserLogable(methodName = "拒绝工单")
    public ResponseData reject(@PathVariable String workOrderId, @RequestParam(defaultValue = "") String rejectReason){
        long id = IdConfuseUtils.decodeId(workOrderId);
        ResponseData responseData = new ResponseData();
        try {
            this.service.rejectWorkOrder(IotpSessionContext.getShopEmployee(), id, rejectReason);
        } catch (ServiceException se) {
            responseData.setReturnCode(ResponseData.ERROR_RETURN_CODE);
            responseData.setReturnMsg(se.getMessage());
        }
        return responseData;
    }

    /**
     * 重新派单
     *
     * @param workOrderId  工单id
     * @param assigneeId   重新分派的服务人员id
     * @return 拒绝结果ResponseData
     */
    @RequestMapping(value = "/{workOrderId}/reassign", method = RequestMethod.POST)
    @ResponseBody
    @UserLogable(methodName = "重新派单")
    public ResponseData reassign(@PathVariable String workOrderId, @RequestParam(defaultValue = "") String assigneeId){
        long id = IdConfuseUtils.decodeId(workOrderId);
        ResponseData responseData = new ResponseData();
        try {
            this.service.reassign(IotpSessionContext.getShopEmployee(), id, IdConfuseUtils.decodeId(assigneeId));
        } catch (ServiceException se) {
            responseData.setReturnCode(ResponseData.ERROR_RETURN_CODE);
            responseData.setReturnMsg(se.getMessage());
        }
        return responseData;
    }

    /**
     * 确认取消工单
     *
     * @param workOrderId  工单id
     * @return 拒绝结果ResponseData
     */
    @RequestMapping(value = "/{workOrderId}/confirmCancel", method = RequestMethod.POST)
    @ResponseBody
    @UserLogable(methodName = "确认取消工单")
    public ResponseData confirmCancel(@PathVariable String workOrderId){
        long id = IdConfuseUtils.decodeId(workOrderId);
        ResponseData responseData = new ResponseData();
        try {
            this.service.confirmCancel(IotpSessionContext.getShopEmployee(), id);
        } catch (ServiceException se) {
            responseData.setReturnCode(ResponseData.ERROR_RETURN_CODE);
            responseData.setReturnMsg(se.getMessage());
        }
        return responseData;
    }
}
