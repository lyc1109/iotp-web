/**
 * Copyright (c) 2015 - Two Steps From Java Group.
 * All rights reserved.
 *
 * Created on 2017-04-21
 */
package io.iotp.api.web.iot;

import io.iotp.iot.service.WaterDeviceService;
import io.iotp.lease.dto.LeaseDeviceDto;
import io.iotp.module.device.entity.Device;
import io.iotp.module.iot.InstructionResult;
import io.iotp.api.IotpEndpoint;
import io.springbootstrap.core.annotation.UserLogConfig;
import io.springbootstrap.core.api.ApiResponseData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * Iot指令管理的Endpoint
 *
 * @author CD826
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/iot/instructions")
@UserLogConfig(moduleName = "Iot指令管理")
@Api(value = "IotInstructionEndpoint", description = "Iot设备管理相关Api")
public class IotInstructionEndpoint extends IotpEndpoint<Device, LeaseDeviceDto> {
    @Autowired
    private WaterDeviceService waterDeviceService;

    /**
     * 获取指令执行的结果
     * @param instructionId 指令的Id
     * @return 获取给定指令执行的结果
     */
    @RequestMapping(value= "/{instructionId}/result", method = RequestMethod.GET)
    @ApiOperation(value = "获取指令执行结果", notes = "获取指令执行结果", httpMethod = "GET", tags = "Web端API -- Iot设备管理")
    public ApiResponseData<InstructionResult> getInstructionResult(@PathVariable @ApiParam(value = "指令Id", required = true) String instructionId) {
        return this.wrap(this.waterDeviceService.getInstructionResult(this.getCurUser(), instructionId));
    }
}
