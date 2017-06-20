/**
 * Copyright (c) 2015 - 广州小橙信息科技有限公司
 * All rights reserved.
 *
 * Created on 2016-11-21
 */
package io.iotp.web.controller.product;

import com.xiaochenghudong.core.util.Constants;
import io.iotp.module.product.entity.ProductSpecGroup;
import io.iotp.module.product.entity.ProductSpecItem;
import io.iotp.product.dto.ProductSpecGroupDto;
import io.iotp.product.dto.ProductSpecInfo;
import io.iotp.product.service.ProductService;
import io.iotp.web.controller.IotpBaseController;
import io.springbootstrap.core.annotation.UserLogConfig;
import io.springbootstrap.core.annotation.UserLogable;
import io.springbootstrap.core.api.ResponseData;
import io.springbootstrap.core.exception.ManageableNotFoundException;
import io.springbootstrap.core.service.SimpleService;
import io.springbootstrap.core.util.CollectionUtils;
import io.springbootstrap.core.util.IdConfuseUtils;
import io.springbootstrap.core.util.NameValuePair;
import io.springbootstrap.core.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;


/**
 * 产品规格模板管理Controller
 *
 * @author CD826
 * @since 1.0.0
 */
@Controller
@RequestMapping("/shop/product/specGroup")
@UserLogConfig(moduleName = "商品规格管理")
public class ProductSpecGroupController extends IotpBaseController<ProductSpecGroup, ProductSpecGroupDto> {

    @Autowired
    private ProductService productService;

    // ========================================================================
    // Page/List Methods ======================================================
    @Override
    public Page<ProductSpecGroupDto> getPageData(@PageableDefault(sort = "created", direction = Sort.Direction.DESC) Pageable pageable, String searchPhrase) {
        return this.productService.getSpecGroupPage(pageable, this.getCurShop().getId(), this.getCfgStatus(), searchPhrase);
    }

    @Override
    protected ProductSpecGroup loadEntity(String entityId) throws ManageableNotFoundException {
        return this.productService.loadProductSpecGroup(IdConfuseUtils.decodeId(entityId));
    }

    @Override
    protected void onOpenForm(Model model, ProductSpecGroupDto dto) {
        super.onOpenForm(model, dto);
        // 设置标准规格列表
        List<NameValuePair> stdSpecs = new ArrayList<>();
        stdSpecs.add(new NameValuePair("颜色"));
        stdSpecs.add(new NameValuePair("尺寸"));
        stdSpecs.add(new NameValuePair("尺码"));
        stdSpecs.add(new NameValuePair("规格"));
        stdSpecs.add(new NameValuePair("款式"));
        stdSpecs.add(new NameValuePair("净含量"));
        stdSpecs.add(new NameValuePair("种类"));
        stdSpecs.add(new NameValuePair("内存"));
        stdSpecs.add(new NameValuePair("版本"));
        stdSpecs.add(new NameValuePair("套餐"));
        stdSpecs.add(new NameValuePair("容量"));
        stdSpecs.add(new NameValuePair("上市时间"));
        stdSpecs.add(new NameValuePair("系列"));
        stdSpecs.add(new NameValuePair("机芯"));
        stdSpecs.add(new NameValuePair("适用"));
        stdSpecs.add(new NameValuePair("包装"));
        stdSpecs.add(new NameValuePair("口味"));
        stdSpecs.add(new NameValuePair("产地"));
        stdSpecs.add(new NameValuePair("介质"));
        this.setAttribute("stdSpecs", stdSpecs);
    }

    @Override
    protected void onOpenEditForm(Model model, ProductSpecGroupDto dto) {
        super.onOpenEditForm(model, dto);

        ProductSpecGroup group = this.productService.loadProductSpecGroup(IdConfuseUtils.decodeId(dto.getId()));
        if(null != group){
            List<ProductSpecItem> items = group.getSpecItemList();
            List<ProductSpecInfo> specInfoList = new ArrayList<>();
            if(!CollectionUtils.isEmpty(items)) {
                for(ProductSpecItem specItem : items) {
                    String[] optionVals = specItem.getItemOptionVals();
                    if (null == optionVals || optionVals.length == 0)
                        continue;
                    specInfoList.add(new ProductSpecInfo(specItem.getName(), optionVals));
                }
            }
            model.addAttribute("specInfoList", specInfoList);
        }
    }

    @Override
    protected ProductSpecGroup saveEntity(ProductSpecGroup productSpecGroup) {
        List<ProductSpecInfo> productSpecInfos = new ArrayList<>();
        String[] specs = this.getStringArrayParameter("specs", null, ";");
        if (!StringUtils.isEmpty(specs)) {
            for(String specItem : specs) {
                String[] specInfos = specItem.split(":");
                if (StringUtils.isEmpty(specInfos) || specInfos.length != 2)
                    continue;

                String specName = specInfos[0];
                if (StringUtils.isNullString(specName))
                    continue;

                String[] items = specInfos[1].split(",");
                if(StringUtils.isEmpty(items))
                    continue;
                productSpecInfos.add(new ProductSpecInfo(specName, items));
            }
        }

        if(productSpecGroup.isNew()){
            productSpecGroup.setStatus(Constants.STATUS_ENABLED);
        }

        productSpecGroup.setShopId(this.getCurShop().getId());
        return this.productService.saveProductSpecGroup(this.getCurUser(), productSpecGroup, productSpecInfos);
    }

    @Override
    protected void deleteEntity(long entityId) {
        this.productService.deleteProductSpecGroup(this.getCurUser(), entityId);
    }

    /**
     * 启用规格模板
     * @param confuseId
     * @return
     */
    @RequestMapping(value = "/{confuseId}/enable", method = RequestMethod.POST)
    @ResponseBody
    @UserLogable(methodName = "启用")
    public ResponseData enable(@PathVariable String confuseId){
        this.productService.enableProductSpecGroup(this.getCurUser(), IdConfuseUtils.decodeId(confuseId));
        return new ResponseData();
    }

    /**
     * 禁用规格模板
     * @param confuseId
     * @return
     */
    @RequestMapping(value = "/{confuseId}/disable", method = RequestMethod.POST)
    @ResponseBody
    @UserLogable(methodName = "禁用")
    public ResponseData disable(@PathVariable String confuseId){
        this.productService.disableProductSpecGroup(this.getCurUser(), IdConfuseUtils.decodeId(confuseId));
        return new ResponseData();
    }
}
