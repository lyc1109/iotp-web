/**
 * Copyright (c) 2015 - Two Steps From Java Group.
 * All rights reserved.
 *
 * Created on 2017-04-21
 */
package io.iotp.api.web.iot;

import com.xiaochenghudong.core.account.entity.User;
import io.iotp.api.IotpEndpoint;
import io.iotp.core.DeviceIdEncoder;
import io.iotp.lease.dto.LeaseDeviceDto;
import io.iotp.module.device.entity.Device;
import io.iotp.module.iot.redis.model.IotDeviceSysLogRedisObject;
import io.iotp.module.iot.redis.repository.IotDeviceRedisRepository;
import io.iotp.module.iot.water.redis.model.WaterDeviceRedisObject;
import io.iotp.module.iot.water.redis.repository.WaterDeviceRedisRepository;
import io.springbootstrap.core.annotation.UserLogConfig;
import io.springbootstrap.core.annotation.UserLogable;
import io.springbootstrap.core.api.ApiResponseData;
import io.springbootstrap.core.util.CollectionUtils;
import io.springbootstrap.core.util.NameValuePair;
import io.springbootstrap.core.util.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Iot设备管理的Endpoint
 *
 * @author CD826
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/v1/admin/iot/devices")
@UserLogConfig(moduleName = "Iot设备管理")
@Api(value = "IotDeviceAdminEndpoint", description = "Iot设备管理相关Api")
public class IotDeviceAdminEndpoint extends IotpEndpoint<Device, LeaseDeviceDto> {
    @Autowired
    private DeviceIdEncoder deviceIdEncoder;
    @Autowired
    private IotDeviceRedisRepository iotDeviceRedisRepository;
    @Autowired
    private WaterDeviceRedisRepository waterDeviceRedisRepository;

    /**
     * 获取符合条件的Iot设备
     * @param searchPhrase
     * @param online
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "获取符合条件的Iot设备", notes = "获取符合条件的Iot设备", httpMethod = "GET", tags = "Web端API -- Iot设备管理")
    @UserLogable(methodName = "获取符合条件的Iot设备")
    public ApiResponseData<List<NameValuePair>> macList(@ApiParam(value = "查询参数", required = true) @RequestParam String searchPhrase,
                                                        @ApiParam(value = "是否只查询在线设备", required = false) @RequestParam(required = false) boolean online) {
        if (StringUtils.isNullString(searchPhrase))
            return new ApiResponseData<>(Collections.EMPTY_LIST);

        // FIXME: 这里需要判断是否是系统管理员
        User curUser = this.getCurUser();

        List<WaterDeviceRedisObject> waterDeviceRedisObjectList = this.waterDeviceRedisRepository.findAllDevices();
        if (CollectionUtils.isEmpty(waterDeviceRedisObjectList)) {
            return new ApiResponseData<>(Collections.EMPTY_LIST);
        }

        // 进行过滤
        List<NameValuePair> deviceList = new ArrayList<>();
        boolean canAdd = false;
        for (WaterDeviceRedisObject waterDeviceRedisObject : waterDeviceRedisObjectList) {
            if (online && !waterDeviceRedisObject.isOnline()) {
                continue;
            }

            canAdd = false;
            String deviceId = this.deviceIdEncoder.encoder(waterDeviceRedisObject.getDeviceId());
            if (waterDeviceRedisObject.getDeviceMac().indexOf(searchPhrase) >= 0) {
                canAdd = true;
            }

            if (!canAdd && deviceId.indexOf(searchPhrase) >= 0) {
                canAdd = true;
            }

            if (canAdd) {
                deviceList.add(new NameValuePair(waterDeviceRedisObject.getDeviceMac() + "(" + deviceId + ")", waterDeviceRedisObject.getDeviceMac()));
            }
        }
        return this.wrap(deviceList);
    }

    /**
     * 获取指定Iot设备的详情
     * @param deviceMac
     * @return
     */
    @RequestMapping(value= "/{deviceMac}", method = RequestMethod.GET)
    @ApiOperation(value = "获取指定Iot设备的详情", notes = "获取指定Iot设备的详情", httpMethod = "GET", tags = "Web端API -- Iot设备管理")
    @UserLogable(methodName = "获取指定Iot设备的详情")
    public ApiResponseData<WaterDeviceRedisObject> detail(@ApiParam(value = "设备的Mac地址") @PathVariable String deviceMac) {
        if (StringUtils.isNullString(deviceMac))
            return new ApiResponseData<>(40001, "参数信息不正确");

        // FIXME: 这里需要判断是否是系统管理员
        User curUser = this.getCurUser();

        WaterDeviceRedisObject waterDeviceRedisObject = this.waterDeviceRedisRepository.loadWaterDeviceByMac(deviceMac);
        if (null == waterDeviceRedisObject) {
            return new ApiResponseData<>(40001, "租赁设备不存在");
        }
        return this.wrap(waterDeviceRedisObject);
    }

    /**
     * 获取Iot设备的即时日志
     * @return
     */
    @RequestMapping(value= "/log", method = RequestMethod.GET)
    @ApiOperation(value = "获取Iot设备的即时日志", notes = "获取Iot设备的即时日志", httpMethod = "GET", tags = "Web端API -- Iot设备管理")
    @UserLogable(methodName = "获取Iot设备的即时日志")
    public ApiResponseData<List<IotDeviceSysLogRedisObject>> log() {
        // FIXME: 这里需要判断是否是系统管理员
        User curUser = this.getCurUser();

        return this.wrap(this.iotDeviceRedisRepository.findAllLastLogs());
    }
}
