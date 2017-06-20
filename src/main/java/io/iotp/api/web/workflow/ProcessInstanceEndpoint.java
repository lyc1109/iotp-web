/**
 * Copyright (c) 2015 - Two Steps From Java Group.
 * All rights reserved.
 *
 * Created on 2017-06-07
 */
package io.iotp.api.web.workflow;

import io.iotp.api.IotpEndpoint;
import io.iotp.workflow.exe.dto.ProcessInstanceDto;
import io.iotp.workflow.exe.dto.TodoWorkDto;
import io.iotp.workflow.service.ProcessInstanceService;
import io.springbootstrap.core.annotation.UserLogConfig;
import io.springbootstrap.core.annotation.UserLogable;
import io.springbootstrap.core.api.ApiPage;
import io.springbootstrap.core.api.ApiResponseData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.twostepsfromjava.workflow.exe.entity.ProcessInstance;

/**
 * 流程实例管理的Endpoint
 *
 * @author CD826
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/workflow/instances")
@UserLogConfig(moduleName = "流程实例管理")
@Api(value = "ProcessInstanceEndpoint", description = "流程实例管理相关Api")
public class ProcessInstanceEndpoint extends IotpEndpoint<ProcessInstance, ProcessInstanceDto> {
    protected Logger logger = LoggerFactory.getLogger(ProcessInstanceEndpoint.class);

    @Autowired
    private ProcessInstanceService processInstanceService;

    /**
     * 获取我的待办列表 -- 首页使用
     * @return
     */
    @RequestMapping(value= "/myTodoList", method = RequestMethod.GET)
    @ApiOperation(value = "获取我的待办列表", notes = "获取我的待办列表 -- 首页使用", httpMethod = "GET", tags = "Web端API -- 流程实例管理")
    @UserLogable(methodName = "获取我的待办列表")
    public ApiResponseData<ApiPage<TodoWorkDto>> myTodoListForHome() {
        int maxCount = 8;
        Pageable pageable = new PageRequest(0, maxCount, Sort.Direction.DESC, "sendedAt");
        return this.wrapPage(this.processInstanceService.getTodoPage(pageable, this.getCurUser(), this.getCurUser().getId(), null));
    }
}
