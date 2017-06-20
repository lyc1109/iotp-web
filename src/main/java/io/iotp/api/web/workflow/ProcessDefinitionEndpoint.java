/**
 * Copyright (c) 2015 - Two Steps From Java Group.
 * All rights reserved.
 *
 * Created on 2017-04-19
 */
package io.iotp.api.web.workflow;

import com.xiaochenghudong.core.account.entity.User;
import io.iotp.core.security.IotpSessionContext;
import io.iotp.lease.dto.LeaseDeviceDto;
import io.iotp.module.device.entity.Device;
import io.iotp.workflow.def.dto.ProcessDefinitionDto;
import io.iotp.workflow.service.ProcessDefinitionService;
import io.springbootstrap.core.annotation.UserLogConfig;
import io.springbootstrap.core.annotation.UserLogable;
import io.springbootstrap.core.api.ApiErrorCode;
import io.springbootstrap.core.api.ApiResponseData;
import io.springbootstrap.core.api.BasicEndpoint;
import io.springbootstrap.core.exception.ServiceException;
import io.springbootstrap.core.util.CollectionUtils;
import io.springbootstrap.core.util.IdConfuseUtils;
import io.springbootstrap.core.util.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.twostepsfromjava.workflow.def.entity.ProcessDefinition;

import java.util.List;

/**
 * FIXME：这里需要增加安全注解
 *
 * 审批流程配置管理的Endpoint
 *
 * @author CD826
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/workflow/definitions")
@UserLogConfig(moduleName = "审批流程配置管理")
@Api(value = "ProcessDefinitionEndpoint", description = "审批流程配置相关Api")
public class ProcessDefinitionEndpoint extends BasicEndpoint<ProcessDefinition, ProcessDefinitionDto> {
    protected Logger logger = LoggerFactory.getLogger(ProcessDefinitionEndpoint.class);

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    /**
     * 获取审批流程配置列表
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "获取审批流程配置列表", notes = "获取审批流程配置列表", httpMethod = "GET", tags = "Web端API -- 审批流程配置")
    @UserLogable(methodName = "获取审批流程配置列表")
    public ApiResponseData<List<ProcessDefinitionDto>> list() {
        User curUser = IotpSessionContext.getCurUser();
        return new ApiResponseData<>(this.processDefinitionService.findAll(curUser, IotpSessionContext.getShopId()));
    }

    /**
     * 获取指定审批流程配置的详细信息
     * @param entityId 审批流程配置的Id(混淆后的)
     * @return 审批流程配置详细信息数据
     */
    @RequestMapping(value= "/{entityId}", method = RequestMethod.GET)
    @ApiOperation(value = "获取指定审批流程配置的详细数据", notes = "获取指定审批流程配置的详细数据", httpMethod = "GET", tags = "Web端API -- 审批流程配置")
    public ApiResponseData<ProcessDefinitionDto> detail(@PathVariable @ApiParam(value = "审批流程配置的Id", required = true) String entityId) {
        long id = IdConfuseUtils.decodeId(entityId);

        ProcessDefinitionDto processDefinitionDto = this.processDefinitionService.load(IotpSessionContext.getCurUser(), id);
        if(null == processDefinitionDto) {
            // FIXME: 错误体系
            return new ApiResponseData<>(40001, "审批流程配置不存在");
        } else {
            return new ApiResponseData<>(processDefinitionDto);
        }
    }

    /**
     * 获取一个空白审批流程配置
     * @param pdTplKey 审批流程配置模版的Key
     * @return 空白审批流程
     */
    @RequestMapping(value= "/loadBlank", method = RequestMethod.GET)
    @ApiOperation(value = "获取一个空白审批流程配置(用于新建)", notes = "获取一个空白审批流程配置(用于新建)", httpMethod = "GET", tags = "Web端API -- 审批流程配置")
    public ApiResponseData<ProcessDefinitionDto> loadBlank(@RequestParam @ApiParam(value = "审批流程配置的Id", required = true) String pdTplKey) {
        ProcessDefinitionDto processDefinitionDto = this.processDefinitionService.loadBlank(IotpSessionContext.getCurUser(), pdTplKey);
        if(null == processDefinitionDto) {
            // FIXME: 错误体系
            return new ApiResponseData<>(40001, "审批流程配置模版不存在");
        } else {
            return new ApiResponseData<>(processDefinitionDto);
        }
    }

    /**
     * 保存/更新审批流程配置
     * @param entityId 所要保存/更新审批流程配置的Id(混淆后的)
     * @param processDefinitionDto 所要保存/更新审批流程配置的数据
     * @return 审批流程配置详细信息数据
     */
    @RequestMapping(value= "/{entityId}", method = RequestMethod.POST)
    @ApiOperation(value = "保存/更新审批流程配置", notes = "保存/更新审批流程配置", httpMethod = "POST", tags = "Web端API -- 审批流程配置")
    @UserLogable(methodName = "更新审批流程配置", description = "流程Key: #{processDefinitionDto.key}")
    public ApiResponseData<ProcessDefinitionDto> save(@PathVariable @ApiParam(value = "审批流程配置的Id(-1表示新建)", required = true) String entityId,
                                                      @RequestBody @ApiParam(value = "审批流程配置的JSON数据", required = true) ProcessDefinitionDto processDefinitionDto) {
        // 获取提交的表单内容
        if (StringUtils.isNullString(processDefinitionDto.getKey())) {
            this.logger.debug("参数不正确，缺少流程配置的Key");
            return new ApiResponseData<>(ApiErrorCode.ILLEGAL_ARGUMENT, "缺少流程配置的Key");
        }

        if (CollectionUtils.isEmpty(processDefinitionDto.getActivityList())) {
            this.logger.debug("参数不正确，缺少流程节点数据");
            return new ApiResponseData<>(ApiErrorCode.ILLEGAL_ARGUMENT, "缺少流程节点配置");
        }

        try {
            processDefinitionDto.setId(entityId);
            processDefinitionDto = this.processDefinitionService.save(IotpSessionContext.getCurUser(), IotpSessionContext.getShopId(), processDefinitionDto);
            return new ApiResponseData<>(processDefinitionDto);
        } catch (ServiceException se) {
            return ApiResponseData.error(se);
        }
    }
}
