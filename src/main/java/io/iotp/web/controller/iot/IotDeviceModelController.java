/*
 * Copyright (c) 2016 - 广东小哈科技股份有限公司 
 * All rights reserved.
 *
 * Created on 2017-02-28
 */
package io.iotp.web.controller.iot;

import io.iotp.core.util.IotDeviceType;
import io.iotp.iot.devicemodel.dto.IotDeviceModelDto;
import io.iotp.iot.devicemodel.dto.IotDeviceModelPartDto;
import io.iotp.iot.devicemodel.service.IotDeviceModelService;
import io.iotp.module.iot.devicemodel.entity.IotDeviceModel;
import io.iotp.module.iot.devicemodel.entity.IotDeviceModelPart;
import io.iotp.web.controller.IotpCrudController;
import io.springbootstrap.core.api.ResponseData;
import io.springbootstrap.core.support.RequestJsonParam;
import io.springbootstrap.core.util.CollectionUtils;
import io.springbootstrap.core.util.IdConfuseUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 智能产品配置管理Controller
 *
 * @author huchiwei
 * @since 2.0.0
 */
@Controller
@RequestMapping("/iot/deviceModel")
public class IotDeviceModelController extends IotpCrudController<IotDeviceModel, IotDeviceModelDto, IotDeviceModelService>{

    @Override
    public Page<IotDeviceModelDto> getPageData(Pageable pageable, String searchPhrase) {
        Page<IotDeviceModel> page = this.service.getPage(pageable, getCurShop().getId(), searchPhrase);
        return this.mapToDtoPage(pageable, page);
    }

    /**
     * 获取智能设备型号列表（下拉选择）
     *
     * @param iotDeviceType 智能设备类型
     * @return 请求结果
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET, headers="X-Requested-With")
    @ResponseBody
    public ResponseData findAll(@RequestParam(defaultValue = IotDeviceType.WATER) String iotDeviceType){
        ResponseData resp = new ResponseData();
        List<IotDeviceModel> iotDeviceModels = this.service.findAll(getCurShop().getId(), iotDeviceType);
        resp.put("iotDeviceModels", this.mapToDtos(iotDeviceModels));
        return resp;
    }

    @Override
    protected void onOpenForm(Model model, IotDeviceModelDto dto) {
        super.onOpenForm(model, dto);

        model.addAttribute("iotDeviceTypes", IotDeviceType.getTypes());
    }

    /**
     * 保存智能产品配置信息
     *
     * @return 请求结果
     */
    @RequestMapping(value = "/{entityId}/save", method = RequestMethod.POST, headers="X-Requested-With")
    @ResponseBody
    public ResponseData saveEntityId(@PathVariable String entityId, @RequestJsonParam("modelParts") List<IotDeviceModelPartDto> modelParts){
        ResponseData resp = new ResponseData();

        IotDeviceModel model = this.service.load(IdConfuseUtils.decodeId(entityId));
        model.setModelName(this.getParameter("modelName", ""));
        model.setMemos(this.getParameter("memos", ""));

        List<IotDeviceModelPart> parts = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(modelParts)) {
            for (IotDeviceModelPartDto dto : modelParts) {
                IotDeviceModelPart part = this.entityDtoMapService.mapToEntity(dto, IotDeviceModelPart.class);
                part.setProductId(IdConfuseUtils.decodeId(dto.getProductId()));
                part.setParent(model);
                parts.add(part);
            }
        }
        this.service.save(model, parts);
        return resp;
    }
}
