/*
 * Copyright (c) 2016 - 广东小哈科技股份有限公司 
 * All rights reserved.
 *
 * Created on 2017-04-10
 */
package io.iotp.web.controller.lease;

import com.xiaochenghudong.core.util.Constants;
import io.iotp.lease.product.dto.LeaseProductDto;
import io.iotp.lease.product.service.LeaseProductService;
import io.iotp.module.lease.product.entity.LeaseProduct;
import io.iotp.module.product.entity.Product;
import io.iotp.module.product.entity.ProductSpec;
import io.iotp.product.dto.ProductSpecDto;
import io.iotp.product.service.ProductService;
import io.iotp.web.controller.IotpCrudController;
import io.springbootstrap.core.annotation.UserLogable;
import io.springbootstrap.core.api.ResponseData;
import io.springbootstrap.core.support.RequestJsonParam;
import io.springbootstrap.core.util.CollectionUtils;
import io.springbootstrap.core.util.IdConfuseUtils;
import io.springbootstrap.core.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 租赁设备管理Controller
 *
 * @author huchiwei
 * @since 2.0.0
 */
@Controller
@RequestMapping("/lease/product")
public class LeaseProductController extends IotpCrudController<LeaseProduct, LeaseProductDto, LeaseProductService>{

    @Autowired
    private ProductService productService;

    @Override
    public Page<LeaseProductDto> getPageData(Pageable pageable, String searchPhrase) {
        int[] status = new int[]{Constants.STATUS_ENABLED, Constants.STATUS_DISABLED};
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.DESC, "created"));
        orders.add(new Sort.Order(Sort.Direction.ASC, "status"));
        Pageable newPageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), new Sort(orders));

        String actionType = this.getParameter("actionType", "grid");
        if (actionType.equalsIgnoreCase("select")) {
            status = new int[]{Constants.STATUS_ENABLED};
        }
        return this.mapToDtoPage(pageable, this.service.getPage(newPageable, getCurShop().getId(), status, searchPhrase));
    }

    @Override
    protected void onSaveEntity(LeaseProduct entity) {
        super.onSaveEntity(entity);

        String productConfuseId = this.getParameter("productConfuseId", "");
        if(StringUtils.hasText(productConfuseId)){
            entity.setProductId(IdConfuseUtils.decodeId(productConfuseId));
        }

        String productSpecConfuseId = this.getParameter("productSpecConfuseId", "");
        if(StringUtils.hasText(productSpecConfuseId)){
            entity.setProductSpecId(IdConfuseUtils.decodeId(productSpecConfuseId));
        }else{
            entity.setProductSpecId(null);
        }

        String[] imageConfuseIds = this.getStringArrayParameter("imageIds", null, ";");
        if(imageConfuseIds.length > 0){
            String imageIds = "";
            for (String imageId : imageConfuseIds) {
                if(StringUtils.hasText(imageIds))
                    imageIds += ";";
                imageIds += IdConfuseUtils.decodeId(imageId);
            }
            entity.setImageIds(imageIds);
        }

        entity.setShopId(getCurShop().getId());
        if (entity.isNew()) {
            entity.setCreated(new Date());
            entity.setModified(new Date());
            entity.setStatus(Constants.STATUS_ENABLED);
        }else{
            entity.setModified(new Date());
        }
    }

    @Override
    protected void onOpenEditForm(Model model, LeaseProductDto dto) {
        super.onOpenEditForm(model, dto);

        boolean isMultiSpec = false;
        Product product = this.productService.load(IdConfuseUtils.decodeId(dto.getProductId()));
        if (null != product && product.isMultiSpec()) {
            isMultiSpec = true;
            List<ProductSpec> productSpecList = this.productService.findProductSpec(product.getId());
            model.addAttribute("productSpecs", this.entityDtoMapService.mapToDtos(productSpecList, ProductSpecDto.class));
        }
        model.addAttribute("isMultiSpec", isMultiSpec);
    }

    /**
     * 选择租赁产品
     *
     * @param model model
     * @return 对话框路径
     */
    @RequestMapping(value = "/select", method = RequestMethod.GET, headers="X-Requested-With")
    public String selectDlg(Model model){
        return this.viewPath("/selectDlg::product4selectGrid");
    }

    /**
     * 上架
     * @param confuseId
     * @return
     */
    @RequestMapping(value = "/{confuseId}/listing", method = RequestMethod.POST)
    @ResponseBody
    @UserLogable(methodName = "上架")
    public ResponseData listing(@PathVariable String confuseId){
        this.service.listing(this.getCurUser(), IdConfuseUtils.decodeId(confuseId));
        return new ResponseData();
    }

    /**
     * 下架
     * @param confuseId
     * @return
     */
    @RequestMapping(value = "/{confuseId}/delisting", method = RequestMethod.POST)
    @ResponseBody
    @UserLogable(methodName = "下架")
    public ResponseData delisting(@PathVariable String confuseId){
        this.service.delisting(this.getCurUser(), IdConfuseUtils.decodeId(confuseId));
        return new ResponseData();
    }

    /**
     * 校验租赁产品名称或代码是否已存在
     *
     * @param name  产品名称
     * @param code  产品代码
     * @return
     */
    @RequestMapping(value = "/isExist", method = RequestMethod.GET, headers="X-Requested-With")
    @ResponseBody
    public ResponseData existItemCode(@RequestParam(defaultValue = "-1") String entityId, @RequestParam(defaultValue = "") String name, @RequestParam(defaultValue = "") String code){
        ResponseData responseData = new ResponseData();

        LeaseProduct leaseProduct= null;
        if (StringUtils.hasText(entityId) && !entityId.equalsIgnoreCase("-1")) {
            leaseProduct = this.service.load(IdConfuseUtils.decodeId(entityId));
        }

        List<LeaseProduct> leaseProducts = this.service.findByName(name);
        if (isExist(leaseProducts, leaseProduct)) {
            responseData.put("isExist", true);
            responseData.put("errorMsg", "租赁产品名称已存在");
            return responseData;
        }

        leaseProducts = this.service.findByCode(code);
        if (isExist(leaseProducts, leaseProduct)) {
            responseData.put("isExist", true);
            responseData.put("errorMsg", "租赁产品代码已存在");
            return responseData;
        }

        responseData.put("isExist", false);
        return responseData;
    }

    /**
     * 判断新增或编辑时是否重复
     *
     * @param leaseProducts  重复记录
     * @param leaseProduct   修改的产品
     * @return
     */
    private boolean isExist(List<LeaseProduct> leaseProducts, LeaseProduct leaseProduct) {
        boolean isExist = false;

        if (CollectionUtils.isNotEmpty(leaseProducts)) {
            if (null != leaseProduct) {
                for (LeaseProduct product : leaseProducts) {
                    if (!Objects.equals(product.getId(), leaseProduct.getId())) {
                        isExist = true;
                        break;
                    }
                }
            } else {
                isExist = true;
            }
        }

        return isExist;
    }
}
