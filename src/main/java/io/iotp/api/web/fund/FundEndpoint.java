/**
 * Copyright (c) 2015 - Two Steps From Java Group.
 * All rights reserved.
 *
 * Created on 2017-04-11
 */
package io.iotp.api.web.fund;

import io.iotp.fund.account.dto.FundAccountDetailDto;
import io.iotp.fund.account.dto.FundAccountDto;
import io.iotp.fund.account.service.FundAccountService;
import io.iotp.module.fund.account.entity.FundAccount;
import io.iotp.module.fund.account.entity.FundAccountDetail;
import io.iotp.api.IotpEndpoint;
import io.springbootstrap.core.annotation.UserLogConfig;
import io.springbootstrap.core.annotation.UserLogable;
import io.springbootstrap.core.api.ApiPage;
import io.springbootstrap.core.api.ApiResponseData;
import io.springbootstrap.core.util.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * FIXME：这里需要增加安全注解
 *
 * 资金管理的Endpoint
 *
 * @author CD826
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/funds")
@UserLogConfig(moduleName = "资金管理")
@Api(value = "FundEndpoint", description = "资金管理相关Api")
public class FundEndpoint extends IotpEndpoint<FundAccount, FundAccountDto> {
    protected Logger logger = LoggerFactory.getLogger(FundEndpoint.class);

    @Autowired
    private FundAccountService fundAccountService;

    /**
     * 获取资金账户基本信息
     * @return
     */
    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    @ApiOperation(value = "获取资金账户基本信息", notes = "获取资金账户基本信息", httpMethod = "GET", tags = "Web端API -- 资金管理")
    @UserLogable(methodName = "获取资金账户基本信息")
    public ApiResponseData<FundAccountDto> dashboard() {
        return this.wrap(this.fundAccountService.loadByShop(this.getCurUser(), this.getCurShopId()));
    }

    /**
     * 资金账户交易明细分页数据
     * @param pageable
     * @param searchPhrase
     * @return
     */
    @RequestMapping(value = "/account/details", method = RequestMethod.GET)
    @ApiOperation(value = "资金账户交易明细分页数据", notes = "资金账户交易明细分页数据", httpMethod = "GET", tags = "Web端API -- 资金管理")
    @UserLogable(methodName = "资金账户交易明细分页数据")
    public ApiResponseData<ApiPage<FundAccountDetailDto>> detailList(@ApiParam(value = "分页参数", required = true) Pageable pageable,
                                                                     @ApiParam(value = "交易类型", required = false) String type,
                                                                     @ApiParam(value = "交易起始时间", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                                                     @ApiParam(value = "交易结束时间", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                                                                     @ApiParam(value = "查询参数", required = false) @RequestParam(required = false) String searchPhrase) {
        String[] types = null;
        if (StringUtils.hasText(type))
            types = new String[]{type};
        Page<FundAccountDetailDto> page = this.fundAccountService.getDetailPage(pageable, this.getCurUser(), this.getCurShopId(), types, startDate, endDate, searchPhrase);
        return this.wrapPage(page);
    }

    /**
     * 获取资金账户最近10条收入
     * @return
     */
    @RequestMapping(value = "/account/last10Incomes", method = RequestMethod.GET)
    @ApiOperation(value = "获取资金账户最近10条收入", notes = "获取资金账户最近10条收入", httpMethod = "GET", tags = "Web端API -- 资金管理")
    @UserLogable(methodName = "最近10条收入")
    public ApiResponseData<List<FundAccountDetailDto>> last10Incomes() {
        String[] types = new String[]{FundAccountDetail.DT_DEPOSIT, FundAccountDetail.DT_INCOME};
        return this.wrap(this.fundAccountService.findLastDetails(this.getCurUser(), this.getCurShopId(), types, 10));
    }

}
