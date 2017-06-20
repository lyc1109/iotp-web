/**
 * Copyright (c) 2015 - 广州小橙信息科技有限公司
 * All rights reserved.
 *
 * Created on 2016-11-21
 */
package io.iotp.web.controller.product;

import com.xiaochenghudong.core.util.Constants;
import io.iotp.module.product.entity.ProductBand;
import io.iotp.product.dto.ProductBandDto;
import io.iotp.product.service.ProductBandService;
import io.iotp.web.controller.IotpCrudController;
import io.springbootstrap.core.annotation.UserLogConfig;
import io.springbootstrap.core.annotation.UserLogable;
import io.springbootstrap.core.api.ResponseData;
import io.springbootstrap.core.util.IdConfuseUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


/**
 * 产品品牌管理Controller
 *
 * @author CD826
 * @since 1.0.0
 */
@Controller
@RequestMapping("/shop/product/band")
@UserLogConfig(moduleName = "商品品牌管理")
public class ProductBandController extends IotpCrudController<ProductBand, ProductBandDto, ProductBandService> {
    // ========================================================================
    // Page/List Methods ======================================================
    @Override
    public Page<ProductBandDto> getPageData(Pageable pageable, String searchPhrase) {
        Pageable newPageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), new Sort(Sort.Direction.DESC, "orderNo"));
        Page<ProductBand> page = this.service.getBandPage(newPageable, getCurShop().getId(), getCfgStatus(), searchPhrase);
        return this.mapToDtoPage(pageable, page);
    }
    @Override
    protected void onSaveEntity(ProductBand entity) {
        super.onSaveEntity(entity);
        entity.setShopId(getCurShop().getId());
    }

    /**
     * 启用品牌
     *
     * @param confuseId 品牌id
     * @return 处理结果
     */
    @RequestMapping(value = "/{confuseId}/enable", method = RequestMethod.POST)
    @ResponseBody
    @UserLogable(methodName = "启用")
    public ResponseData enable(@PathVariable String confuseId){
        this.service.enable(IdConfuseUtils.decodeId(confuseId));
        return new ResponseData();
    }

    /**
     * 禁用品牌
     *
     * @param confuseId 品牌id
     * @return 处理结果
     */
    @RequestMapping(value = "/{confuseId}/disable", method = RequestMethod.POST)
    @ResponseBody
    @UserLogable(methodName = "禁用")
    public ResponseData disable(@PathVariable String confuseId){
        this.service.disable(IdConfuseUtils.decodeId(confuseId));
        return new ResponseData();
    }

    /**
     * 判断品牌名称是否已存在
     *
     * @param name 品牌名称
     * @return 处理结果
     */
    @RequestMapping(value = "/isExist", method = RequestMethod.GET)
    @ResponseBody
    public ResponseData isExist(@RequestParam String name){
        ResponseData responseData = new ResponseData();
        ProductBand productBand = this.service.findOne(getCurShop().getId(), name);
        responseData.put("isExist", null != productBand && productBand.getStatus() != Constants.STATUS_DELETED);
        return responseData;
    }

}
