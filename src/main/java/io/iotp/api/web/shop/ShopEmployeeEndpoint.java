/**
 * Copyright (c) 2015 - Two Steps From Java Group.
 * All rights reserved.
 *
 * Created on 2017-05-08
 */
package io.iotp.api.web.shop;

import io.iotp.api.IotpEndpoint;
import io.iotp.module.shop.entity.ShopEmployee;
import io.iotp.shop.dto.ShopEmployeeDto;
import io.iotp.shop.service.ShopEmployeeService;
import io.springbootstrap.core.annotation.UserLogConfig;
import io.springbootstrap.core.annotation.UserLogable;
import io.springbootstrap.core.api.ApiResponseData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * FIXME：这里需要增加安全注解
 *
 * 店铺员工管理的Endpoint
 *
 * @author CD826
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/shop/employees")
@UserLogConfig(moduleName = "店铺员工管理")
@Api(value = "ShopEmployeeEndpoint", description = "店铺员工管理相关Api")
public class ShopEmployeeEndpoint extends IotpEndpoint<ShopEmployee, ShopEmployeeDto> {
    @Autowired
    private ShopEmployeeService shopEmployeeService;

    /**
     * 获取当前店铺中所有员工信息
     * @param searchPhrase 查询条件
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "获取店铺员工信息列表", notes = "获取店铺员工信息列表", httpMethod = "GET", tags = "Web端API -- 店铺员工管理")
    @UserLogable(methodName = "获取店铺员工信息列表")
    public ApiResponseData<List<ShopEmployeeDto>> findAllEmployees(@ApiParam(value = "查询条件", required = false) @RequestParam(required = false) String searchPhrase) {
        Long shopId = this.getCurShopId();
        List<ShopEmployee> shopEmployees = this.shopEmployeeService.findAllByShop(shopId, searchPhrase);
        return this.wrap(this.mapToDtos(shopEmployees));
    }
}
