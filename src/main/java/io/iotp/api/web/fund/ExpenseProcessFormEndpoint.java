/**
 * Copyright (c) 2015 - Two Steps From Java Group.
 * All rights reserved.
 *
 * Created on 2017-04-21
 */
package io.iotp.api.web.fund;

import io.iotp.fund.account.dto.FundAccountDto;
import io.iotp.fund.form.ExpenseProcessFormContext;
import io.iotp.fund.form.dto.ExpenseProcessFormDto;
import io.iotp.fund.form.service.ExpenseProcessFormService;
import io.iotp.module.fund.account.entity.FundAccount;
import io.iotp.module.fund.form.entity.ExpenseProcessForm;
import io.iotp.api.IotpEndpoint;
import io.springbootstrap.core.annotation.UserLogConfig;
import io.springbootstrap.core.annotation.UserLogable;
import io.springbootstrap.core.api.ApiPage;
import io.springbootstrap.core.api.ApiResponseData;
import io.springbootstrap.core.exception.ServiceException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;


/**
 * 支出审批管理的Endpoint
 *
 * @author CD826
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/funds/expenseform")
@UserLogConfig(moduleName = "支出审批管理")
@Api(value = "ExpenseProcessFormEndpoint", description = "支出审批管理相关Api")
public class ExpenseProcessFormEndpoint extends IotpEndpoint<FundAccount, FundAccountDto> {
    // ========================================================================
    // constants ==============================================================
    /** 视图类别: 审批中, 值为:{@value} */
    private static final String VT_PROCESSING      				    = "processing";

    @Autowired
    private ExpenseProcessFormService expenseProcessFormService;

    /**
     * 获取支出审批的分页数据
     * @param pageable
     * @param searchPhrase
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "获取支出审批单分页数据", notes = "获取支出审批单分页数据", httpMethod = "GET", tags = "Web端API -- 支出审批管理")
    @UserLogable(methodName = "获取支出审批单分页数据")
    public ApiResponseData<ApiPage<ExpenseProcessFormDto>> list(@ApiParam(value = "分页参数", required = true) Pageable pageable,
                                                                @ApiParam(value = "视图类型", required = false) @RequestParam(required = false) String viewType,
                                                                @ApiParam(value = "查询参数", required = false) @RequestParam(required = false) String searchPhrase) {
        int[] status = null;
        if (VT_PROCESSING.equalsIgnoreCase(viewType)) { // 审批中
            status = new int[]{ExpenseProcessForm.PIS_START, ExpenseProcessForm.PIS_FLOW};
        }
        Page<ExpenseProcessFormDto> page = this.expenseProcessFormService.getPage(pageable, this.getCurUser(), this.getCurShopId(), status, searchPhrase);
        return this.wrapPage(page);
    }

    /**
     * 获取指定支出审批单的详细信息
     * @param entityId 支出审批单的Id(混淆后的)
     * @return 支出审批单详细信息数据
     */
    @RequestMapping(value= "/{entityId}", method = RequestMethod.GET)
    @ApiOperation(value = "获取支出审批单详细信息", notes = "获取支出审批单详细信息", httpMethod = "GET", tags = "Web端API -- 支出审批管理")
    public ApiResponseData<ExpenseProcessFormDto> detail(@PathVariable @ApiParam(value = "支出审批单的Id", required = true) String entityId) {
        ExpenseProcessFormDto expenseProcessFormDto = this.expenseProcessFormService.load(this.getCurUser(), this.decodeEntityId(entityId));
        if(null == expenseProcessFormDto) {
            // FIXME: 错误体系
            return new ApiResponseData<>(40001, "支出审批单不存在");
        } else {
            return this.wrap(expenseProcessFormDto);
        }
    }

    /**
     * 获取一个空白的支出审批表
     * @return 空白支出审批表
     */
    @RequestMapping(value= "/loadBlank", method = RequestMethod.GET)
    @ApiOperation(value = "获取一个空白的支出审批表(用于新建)", notes = "获取一个空白的支出审批表(用于新建)", httpMethod = "GET", tags = "Web端API -- 支出审批管理")
    public ApiResponseData<ExpenseProcessFormDto> loadBlank() {
        return this.wrap(this.expenseProcessFormService.loadBlank(this.getCurUser(), this.getCurShopId()));
    }

    /**
     * 创建支出审批单
     * @param formContext 审批单中的内容
     * @param nextActorId 下一办理人，如果为空表示从流程定义中获取
     * @return
     */
    @RequestMapping(value= "/submit", method = RequestMethod.POST)
    @ApiOperation(value = "提交一个新的支出审批单", notes = "提交一个新的支出审批单", httpMethod = "POST", tags = "Web端API -- 支出审批管理")
    public ApiResponseData submit(@RequestBody @ApiParam(value = "支出审批表单的JSON数据", required = true) ExpenseProcessFormContext formContext,
                                  @RequestParam @ApiParam(value = "下一步骤办理人Id", required = true) String nextActorId) {
        try {
            ExpenseProcessForm form = this.expenseProcessFormService.startProcess(this.getCurUser(), this.getCurShopId(), formContext, this.decodeEntityId(nextActorId));
            if (null != form) {
                ApiResponseData apiResponseData = new ApiResponseData();
                apiResponseData.setData(form);
                return apiResponseData;
            }else
                return ApiResponseData.error("创建支出审批单出错");
        } catch (ServiceException se) {
            return ApiResponseData.error(se);
        }
    }

    /**
     * 送下一个办理人
     * @param entityId 所办理的支出审批单的Id(混淆后)
     * @param nextActorId 下一办理人，如果为空表示从流程定义中获取
     * @param comment 办理意见
     * @return
     */
    @RequestMapping(value= "/{entityId}/signal", method = RequestMethod.POST)
    @ApiOperation(value = "送下一个办理人", notes = "送下一个办理人", httpMethod = "POST", tags = "Web端API -- 支出审批管理")
    @UserLogable(methodName = "送下一个办理人", description = "'审批单为:' + #entityId + '，办理人为: ' + #nextActorId")
    public ApiResponseData signal(@PathVariable @ApiParam(value = "租赁设备的Id", required = true) String entityId,
                                  @RequestParam @ApiParam(value = "下一步骤办理人Id", required = true) String nextActorId,
                                  @RequestParam @ApiParam(value = "审批说明", required = true) String comment) {
        try {
            this.expenseProcessFormService.signal(this.getCurUser(), this.decodeEntityId(entityId), this.decodeEntityId(nextActorId), comment);
            return new ApiResponseData();
        } catch (ServiceException se) {
            return ApiResponseData.error(se);
        }
    }

    /**
     * 审批通过
     * @param entityId 所办理的支出审批单的Id(混淆后)
     * @param comment 办理意见
     * @return
     */
    @RequestMapping(value= "/{entityId}/approve", method = RequestMethod.POST)
    @ApiOperation(value = "审批通过", notes = "审批通过", httpMethod = "POST", tags = "Web端API -- 支出审批管理")
    @UserLogable(methodName = "审批通过", description = "'审批单为:' + #entityId")
    public ApiResponseData approve(@PathVariable @ApiParam(value = "租赁设备的Id", required = true) String entityId,
                                   @RequestParam @ApiParam(value = "审批说明", required = true) String comment) {
        try {
            this.expenseProcessFormService.approve(this.getCurUser(), this.decodeEntityId(entityId), comment);
            return new ApiResponseData();
        } catch (ServiceException se) {
            return ApiResponseData.error(se);
        }
    }

    /**
     * 驳回/审批不通过
     * @param entityId 所办理的支出审批单的Id(混淆后)
     * @param comment 办理意见
     * @return
     */
    @RequestMapping(value= "/{entityId}/reject", method = RequestMethod.POST)
    @ApiOperation(value = "审批不通过", notes = "审批不通过", httpMethod = "POST", tags = "Web端API -- 支出审批管理")
    @UserLogable(methodName = "审批不通过", description = "'审批单为:' + #entityId")
    public ApiResponseData reject(@PathVariable @ApiParam(value = "租赁设备的Id", required = true) String entityId,
                                  @RequestParam @ApiParam(value = "审批说明", required = true) String comment) {
        try {
            this.expenseProcessFormService.reject(this.getCurUser(), this.decodeEntityId(entityId), comment);
            return new ApiResponseData();
        } catch (ServiceException se) {
            return ApiResponseData.error(se);
        }
    }
}
