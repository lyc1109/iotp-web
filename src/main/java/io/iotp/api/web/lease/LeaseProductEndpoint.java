/**
 * Copyright (c) 2015 - Two Steps From Java Group.
 * All rights reserved.
 *
 * Created on 2017-04-19
 */
package io.iotp.api.web.lease;

import com.xiaochenghudong.core.account.entity.User;
import com.xiaochenghudong.core.util.Constants;
import io.iotp.core.security.IotpSessionContext;
import io.iotp.lease.dto.LeaseDeviceDto;
import io.iotp.lease.product.service.LeaseProductService;
import io.iotp.module.device.entity.Device;
import io.iotp.module.lease.product.entity.LeaseProduct;
import io.iotp.module.shop.entity.Shop;
import io.springbootstrap.core.annotation.UserLogConfig;
import io.springbootstrap.core.annotation.UserLogable;
import io.springbootstrap.core.api.ApiResponseData;
import io.springbootstrap.core.api.BasicEndpoint;
import io.springbootstrap.core.util.CollectionUtils;
import io.springbootstrap.core.util.NameValuePair;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 租赁产品配置管理的Endpoint
 *
 * @author CD826
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/lease/products")
@UserLogConfig(moduleName = "租赁产品配置管理")
@Api(value = "LeaseProductEndpoint", description = "租赁产品配置管理相关Api")
public class LeaseProductEndpoint extends BasicEndpoint<Device, LeaseDeviceDto> {
    protected Logger logger = LoggerFactory.getLogger(LeaseProductEndpoint.class);

    @Autowired
    private LeaseProductService leaseProductService;

    /**
     * 获取所配置的租赁产品选项列表
     * @return
     */
    @RequestMapping(value = "/listOptions", method = RequestMethod.GET)
    @ApiOperation(value = "获取所配置的租赁产品选项列表", notes = "获取所配置的租赁产品选项列表", httpMethod = "GET", tags = "Web端API -- 租赁产品配置管理")
    @UserLogable(methodName = "获取所配置的租赁产品选项列表")
    public ApiResponseData<List<NameValuePair>> listOptions() {
        User curUser = IotpSessionContext.getCurUser();
        Shop curShop = IotpSessionContext.getShop();

        List<LeaseProduct> leaseProducts = this.leaseProductService.findAll(curShop.getId());
        List<NameValuePair> listOptions = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(leaseProducts)) {
            for (LeaseProduct leaseProduct : leaseProducts) {
                if (leaseProduct.getStatus() == Constants.STATUS_ENABLED || leaseProduct.getStatus() == Constants.STATUS_DISABLED)
                    listOptions.add(new NameValuePair(leaseProduct.getName(), leaseProduct.getConfuseId()));
            }
        }
        return new ApiResponseData<>(listOptions);
    }
}
