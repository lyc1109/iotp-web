/*
 * Copyright (c) 2016 - 广东小哈科技股份有限公司 
 * All rights reserved.
 *
 * Created on 2017-04-06
 */
package io.iotp.web.controller.shop;

import io.iotp.core.util.DeviceType;
import io.iotp.core.util.IotDeviceType;
import io.iotp.member.device.dto.UserDeviceDto;
import io.iotp.member.device.service.UserDeviceService;
import io.iotp.module.device.entity.Device;
import io.iotp.module.device.entity.UserDevice;
import io.iotp.web.controller.IotpCrudController;
import io.springbootstrap.core.util.IdConfuseUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 用户设备Controller
 *
 * @author huchiwei
 * @since 2.0.0
 */
@Controller
@RequestMapping("/shop/member/devices")
public class UserDeviceController extends IotpCrudController<UserDevice, UserDeviceDto, UserDeviceService> {

    @Override
    public Page<UserDeviceDto> getPageData(Pageable pageable, String searchPhrase) {
        String userId = this.getParameter("userId", "");
        String deviceType = this.getParameter("deviceType", "");
        Page<UserDevice> page = this.service.getPage(pageable, getCurShop().getId(), IdConfuseUtils.decodeId(userId), deviceType, searchPhrase);
        return this.mapToDtoPage(pageable, page);
    }

    /**
     * 选择商品
     *
     * @param model model
     * @return 对话框路径
     */
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public String selectProductDlg(Model model){
        return this.viewPath("/selectDeviceDlg::device4selectGrid");
    }

    @RequestMapping(value = "/{deviceId}/view",method = RequestMethod.GET)
    public String viewDevices(Model model,@PathVariable String deviceId){
        UserDevice userDevice = this.service.load(IdConfuseUtils.decodeId(deviceId));
        Device device = userDevice.getDevice();
        String viewPath = this.viewPath("/view");
        if(device.getDeviceType().equalsIgnoreCase(DeviceType.INTELLIGENT)){
            if(device.getIotDeviceType().equalsIgnoreCase(IotDeviceType.WATER)){
               //净水器
            }
            else if(device.getIotDeviceType().equalsIgnoreCase(IotDeviceType.AIR_CLEANER)){
                // 空气净化器

            }
        }
        else if(device.getDeviceType().equalsIgnoreCase(DeviceType.LEASE)){
            if(device.getIotDeviceType().equalsIgnoreCase(IotDeviceType.WATER)){
                //租赁
                viewPath = "redirect:/lease/devices/" + deviceId;
            }
        }
        model.addAttribute("entity",this.mapToDto(userDevice));
        return viewPath;
    }
}
