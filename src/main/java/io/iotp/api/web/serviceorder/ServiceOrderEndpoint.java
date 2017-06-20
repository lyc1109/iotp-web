/**
 * Copyright (c) 2015 - Two Steps From Java Group.
 * All rights reserved.
 *
 * Created on 2017-06-07
 */
package io.iotp.api.web.serviceorder;

import io.iotp.api.IotpEndpoint;
import io.iotp.module.serviceorder.entity.ServiceOrder;
import io.iotp.serviceorder.dto.ServiceOrderDto;
import io.iotp.serviceorder.service.ServiceOrderService;
import io.springbootstrap.core.annotation.UserLogConfig;
import io.springbootstrap.core.annotation.UserLogable;
import io.springbootstrap.core.api.ApiPage;
import io.springbootstrap.core.api.ApiResponseData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 服务订单管理的Endpoint
 *
 * @author CD826
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/serviceOrders")
@UserLogConfig(moduleName = "服务订单管理")
@Api(value = "ServiceOrderEndpoint", description = "服务订单管理相关Api")
public class ServiceOrderEndpoint extends IotpEndpoint<ServiceOrder, ServiceOrderDto> {
    @Autowired
    private ServiceOrderService serviceOrderService;

    /**
     * 获取订单信息列表
     * @param pageable
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "获取订单信息列表", notes = "获取订单信息列表", httpMethod = "GET", tags = "Web端API -- 订单管理")
    @UserLogable(methodName = "获取订单信息列表")
    public ApiResponseData<List<ServiceOrderDto>> list(@ApiParam(value = "分页参数", required = true) Pageable pageable) {
        Long shopId = this.getCurShopId();
        // List<ShopEmployee> shopEmployees = this.shopEmployeeService.findAllByShop(shopId, searchPhrase);
        // return this.wrap(this.mapToDtos(shopEmployees));
        return new ApiResponseData<>();
    }

    /**
     * 获取订单信息列表
     * @param viewType
     * @return
     */
    @RequestMapping(value= "/{viewType}", method = RequestMethod.GET)
    @ApiOperation(value = "获取订单信息列表", notes = "获取订单信息列表 -- 首页使用", httpMethod = "GET", tags = "Web端API -- 订单管理")
    @UserLogable(methodName = "获取订单信息列表")
    public ApiResponseData<ApiPage<ServiceOrderDto>> listForHome(@ApiParam(value = "视图类型", required = true) @PathVariable String viewType) {
        int maxCount = 8;
        Long shopId = this.getCurShopId();
        Pageable pageable = new PageRequest(0, maxCount, Sort.Direction.DESC, "created");
        Page<ServiceOrder> page = null;
        if ("processing".equalsIgnoreCase(viewType)) {
            int[] status = new int[]{ServiceOrder.SOT_ASSIGNED, ServiceOrder.SOT_PROCESSING_UNPAY, ServiceOrder.SOT_PROCESSING_PAIED};
            page = this.serviceOrderService.getPage(pageable, shopId, status, null, null);
        } else if ("newest".equalsIgnoreCase(viewType)) {
            page = this.serviceOrderService.getPage(pageable, shopId, null, null, null);
        } else {
            return this.blankPage();
        }
        return this.wrapPage(this.mapToDtoPage(pageable, page));
    }
}
