/**
 * Copyright (c) 2015 - Two Steps From Java Group.
 * All rights reserved.
 *
 * Created on 2017-05-22
 */
package io.iotp.api.web.lease;

import io.iotp.api.IotpEndpoint;
import io.iotp.lease.dto.RechargeOrderDto;
import io.iotp.lease.service.LeaseService;
import io.iotp.module.lease.order.entity.RechargeOrder;
import io.springbootstrap.core.annotation.UserLogConfig;
import io.springbootstrap.core.api.ApiResponseData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * FIXME：这里需要增加安全注解
 *
 * 充值订单管理的Endpoint
 *
 * @author CD826
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/lease/rechargeorders")
@UserLogConfig(moduleName = "充值订单管理")
@Api(value = "RechargeOrderEndpoint", description = "租赁管理相关Api")
public class RechargeOrderEndpoint extends IotpEndpoint<RechargeOrder, RechargeOrderDto> {
    @Autowired
    private LeaseService leaseService;

    /**
     * 获取指定充值订单的详细信息
     * @param entityId 充值订单的Id(混淆后的)
     * @return 充值订单详细信息数据
     */
    @RequestMapping(value= "/{entityId}", method = RequestMethod.GET)
    @ApiOperation(value = "获取指定充值订单的详细信息", notes = "获取指定充值订单的详细信息", httpMethod = "GET", tags = "Web端API -- 充值订单管理")
    public ApiResponseData<RechargeOrderDto> detail(@PathVariable @ApiParam(value = "充值订单的Id", required = true) String entityId) {
        RechargeOrderDto rechargeOrderDto = this.leaseService.loadRechargeOrder(this.decodeEntityId(entityId));
        if(null == rechargeOrderDto) {
            // FIXME: 错误体系
            return new ApiResponseData<>(40001, "充值订单不存在");
        } else {
            return this.wrap(rechargeOrderDto);
        }
    }
}
