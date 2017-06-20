/**
 * Copyright (c) 2015 - 广州小橙信息科技有限公司
 * All rights reserved.
 *
 * Created on 2017-03-16
 */
package io.iotp.web.controller.system;

import io.iotp.core.security.IotpSessionContext;
import io.iotp.iot.service.WaterDeviceService;
import io.iotp.lease.service.LeaseDeviceService;
import io.iotp.member.device.service.UserDeviceService;
import io.iotp.module.device.entity.UserDevice;
import io.iotp.module.iot.InstructionResult;
import io.springbootstrap.core.api.ResponseData;
import io.springbootstrap.core.controller.AbstractController;
import io.springbootstrap.core.exception.ServiceException;
import io.springbootstrap.core.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * 工具Controller
 *
 * @author CD826
 * @since 2.0.0
 */
@Controller
@RequestMapping("/tools")
public class ToolsController extends AbstractController {
    @Autowired
    private UserDeviceService userDeviceService;
    @Autowired
    private LeaseDeviceService leaseDeviceService;
    @Autowired
    private WaterDeviceService waterDeviceService;

    /**
     * Tools页面
     */
    @RequestMapping(method = RequestMethod.GET)
    public String index(){
        return this.viewPath("/index");
    }

    /**
     * 重新加载Iot设备信息到Redis数据库中
     * @return
     */
    @RequestMapping(value={"/reloadIotDevices"}, method= RequestMethod.GET)
    @ResponseBody
    public String reloadIotDevices() {
        // this.iotDeviceWarehouse.reload();
        return "Reload Ok!";
    }

    /**
     * 注册Iot设备
     */
    @RequestMapping(value = "registerIotDevicePage", method = RequestMethod.GET)
    public String registerIotDevicePage(){
        return this.viewPath("/registerIotDevice");
    }

    /**
     * 注册Iot设备
     * @param deviceId
     * @param macAddress
     * @param mobile
     * @return
     */
    @RequestMapping(value = "registerIotDevice", method = RequestMethod.POST)
    @ResponseBody
    public ResponseData registerIotDevice(@RequestParam("deviceId") String deviceId, @RequestParam("macAddress") String macAddress,
                                          @RequestParam("mobile") String mobile){
        try {
            UserDevice userDevice = this.userDeviceService.registerIotDevice(deviceId, macAddress, null, mobile);
            if (null == userDevice) {
                return new ResponseData(ResponseData.ERROR_RETURN_CODE, "注册设备失败");
            } else {
                ResponseData responseData = new ResponseData();
                responseData.setReturnMsg("注册设备成功");
                return responseData;
            }
        } catch (ServiceException se) {
            return new ResponseData(ResponseData.ERROR_RETURN_CODE, se.getMessage());
        }
    }


    /**
     * 注册一个租赁设备
     */
    @RequestMapping(value = "registerLeaseDevicePage", method = RequestMethod.GET)
    public String registerLeaseDevicePage(){
        return this.viewPath("/registerLeaseDevice");
    }

    /**
     * 注册Iot设备
     * @param deviceId
     * @param macAddress
     * @param mobile
     * @return
     */
    @RequestMapping(value = "registerLeaseDevice", method = RequestMethod.POST)
    @ResponseBody
    public ResponseData registerLeaseDevice(@RequestParam("deviceId") String deviceId, @RequestParam("macAddress") String macAddress,
                                            @RequestParam("mobile") String mobile){
        try {
            UserDevice userDevice = this.userDeviceService.registerIotDevice(deviceId, macAddress, null, mobile);
            if (null == userDevice) {
                return new ResponseData(ResponseData.ERROR_RETURN_CODE, "注册设备失败");
            } else {
                // 激活该设备
                this.leaseDeviceService.activateLeaseTestDevice(IotpSessionContext.getCurUser(), userDevice.getId());
                ResponseData responseData = new ResponseData();
                responseData.setReturnMsg("注册租赁设备成功");
                return responseData;
            }
        } catch (ServiceException se) {
            return new ResponseData(ResponseData.ERROR_RETURN_CODE, se.getMessage());
        }
    }

    /**
     * 删除Iot设备
     */
    @RequestMapping(value = "unregisterIotDevicePage", method = RequestMethod.GET)
    public String unregisterIotDevicePage(){
        return this.viewPath("/unregisterIotDevice");
    }

    /**
     * 删除Iot设备
     * @param macAddress
     * @return
     */
    @RequestMapping(value = "unregisterIotDevice", method = RequestMethod.POST)
    @ResponseBody
    public ResponseData unregisterIotDevice(@RequestParam("macAddress") String macAddress){
        if (StringUtils.isNullString(macAddress)) {
            return new ResponseData(ResponseData.ERROR_RETURN_CODE, "未获取到所要删除的设备Mac地址");
        }
        String[] macAddresses = macAddress.split(",");
        if (StringUtils.isEmpty(macAddresses)) {
            return new ResponseData(ResponseData.ERROR_RETURN_CODE, "未获取到所要删除的设备Mac地址");
        }

        try {
            this.userDeviceService.unregisterIotDevice(IotpSessionContext.getCurUser(), macAddresses);
            return new ResponseData();
        } catch (ServiceException se) {
            return new ResponseData(ResponseData.ERROR_RETURN_CODE, se.getMessage());
        }
    }

    /**
     * 重置设备滤芯
     */
    @RequestMapping(value = "resetFilterPage", method = RequestMethod.GET)
    public String resetFilterPage(){
        return this.viewPath("/resetFilter");
    }

    /**
     * 重置设备的滤芯
     * @param iotModel
     * @param macAddress
     * @return
     */
    @RequestMapping(value = "resetFilter", method = RequestMethod.POST)
    @ResponseBody
    public ResponseData resetFilter(@RequestParam("iotModel") String iotModel, @RequestParam("macAddress") String macAddress){
        String[] macAddresses = null;
        if (!StringUtils.isNullString(macAddress)) {
            macAddresses = macAddress.split(",");
        }

        try {
            this.waterDeviceService.resetFilterPart(IotpSessionContext.getCurUser(), iotModel, macAddresses);
            return new ResponseData();
        } catch (ServiceException se) {
            return new ResponseData(ResponseData.ERROR_RETURN_CODE, se.getMessage());
        }
    }

    /**
     * 重置Iot设备的数据
     */
    @RequestMapping(value = "resetIotDeviceDataPage", method = RequestMethod.GET)
    public String resetIotDeviceDataPage(){
        return this.viewPath("/resetIotDeviceData");
    }

    /**
     * 重置Iot设备的数据
     * @param macAddress
     * @return
     */
    @RequestMapping(value = "resetIotDeviceData", method = RequestMethod.POST)
    @ResponseBody
    public ResponseData resetIotDevice(@RequestParam("macAddress") String macAddress){
        if (StringUtils.isNullString(macAddress)) {
            return new ResponseData(ResponseData.ERROR_RETURN_CODE, "未获取到所要重置设备Mac地址");
        }
        String[] macAddresses = macAddress.split(",");
        if (StringUtils.isEmpty(macAddresses)) {
            return new ResponseData(ResponseData.ERROR_RETURN_CODE, "未获取到所要重置设备Mac地址");
        }

        try {
            this.waterDeviceService.resetDeviceData(IotpSessionContext.getCurUser(), macAddresses);
            return new ResponseData();
        } catch (ServiceException se) {
            return new ResponseData(ResponseData.ERROR_RETURN_CODE, se.getMessage());
        }
    }

    /**
     * 向指定Iot设备发送指令
     */
    @RequestMapping(value = "execuInstructionPage", method = RequestMethod.GET)
    public String execuInstructionPage(){
        return this.viewPath("/execuInstruction");
    }

    /**
     * 向指定Iot设备发送指令
     * @param macAddress
     * @param instructionCode
     * @return
     */
    @RequestMapping(value = "execuInstruction", method = RequestMethod.POST)
    @ResponseBody
    public ResponseData execuInstruction(@RequestParam("macAddress") String macAddress, @RequestParam("instructionCode") String instructionCode){
        if (StringUtils.isNullString(macAddress)) {
            return new ResponseData(ResponseData.ERROR_RETURN_CODE, "未获取到所要Iot设备Mac地址");
        }

        try {
            String instructionId = this.waterDeviceService.executeInstructionByMac(IotpSessionContext.getCurUser(), macAddress, instructionCode);
            return new ResponseData("instructionId", instructionId);
        } catch (ServiceException se) {
            return new ResponseData(ResponseData.ERROR_RETURN_CODE, se.getMessage());
        }
    }

    /**
     * 获取指令执行结果
     *
     * @return 指令执行结果
     */
    @RequestMapping(value = "/getInstructionResult", method = RequestMethod.GET, headers="X-Requested-With")
    @ResponseBody
    public ResponseData fetchInstructionResult(@RequestParam String instructionId){
        ResponseData responseData = new ResponseData();
        try {
            InstructionResult instructionResult = this.waterDeviceService.getInstructionResult(IotpSessionContext.getCurUser(), instructionId);
            responseData.put("instructionResult", instructionResult);
        } catch (ServiceException e) {
            e.printStackTrace();
            responseData = new ResponseData(ResponseData.ERROR_RETURN_CODE, e.getMessage());
        }
        return responseData;
    }

    /**
     * iot设备详情
     */
    @RequestMapping(value = "/IotDeviceDetail", method = RequestMethod.GET)
    public String iotDeviceDetail(){
        return this.viewPath("/iotDeviceDetail");
    }

    /**
     * Iot设备日志查看器
     */
    @RequestMapping(value = "/IotDeviceLog", method = RequestMethod.GET)
    public String iotDeviceLog(){
        return this.viewPath("/iotDeviceLog");
    }


    /**
     * 短信发送记录查询
     */
    @RequestMapping(value = "sms", method = RequestMethod.GET)
    public String sms(){
        return this.viewPath("/sms");
    }
}