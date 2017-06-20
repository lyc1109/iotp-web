/**
 * Copyright (c) 2015 - 广州小橙信息科技有限公司
 * All rights reserved.
 *
 * Created on 2016-11-21
 */
package io.iotp.web.controller.product;

import com.xiaochenghudong.core.util.Constants;
import io.iotp.product.dto.ProductCategoryDto;
import io.iotp.module.product.entity.ProductCategory;
import io.iotp.product.service.ProductCategoryService;
import io.iotp.web.controller.IotpCrudController;
import io.springbootstrap.core.annotation.UserLogConfig;
import io.springbootstrap.core.api.ResponseData;
import io.springbootstrap.core.util.IdConfuseUtils;
import io.springbootstrap.core.util.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * 产品分类管理Controller
 *
 * @author CD826
 * @since 1.0.0
 */
@Controller
@RequestMapping("/shop/product/category")
@UserLogConfig(moduleName = "商品分类管理")
public class ProductCategoryController extends IotpCrudController<ProductCategory, ProductCategoryDto, ProductCategoryService> {
    // ========================================================================
    // Page/List Methods ======================================================
    @Override
    public Page<ProductCategoryDto> getPageData(Pageable pageable, String searchPhrase) {
        Pageable newPageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), new Sort(Sort.Direction.DESC, "orderNo"));
        Page<ProductCategory> page = this.service.getPage(newPageable, getCurShop().getId(), getCfgStatus(), searchPhrase);
        return this.mapToDtoPage(pageable, page);
    }

    @Override
    protected void onOpenCreateForm(Model model, ProductCategoryDto dto) {
        String parentId =  this.getParameter("parentId", "");
        if (StringUtils.hasText(parentId)) {
            ProductCategory category = this.service.load(IdConfuseUtils.decodeId(parentId));
            if(null != category){
                dto.setParentId(category.getConfuseId());
                dto.setParentName(category.getFullName());
            }
        }
    }

    @Override
    protected void onSaveEntity(ProductCategory productCategory) {
        super.onSaveEntity(productCategory);

        long parentId = IdConfuseUtils.decodeId(this.getParameter("parentId", ""));
        if (parentId > 0) {
            productCategory.setParent(this.service.load(parentId));
        }

        productCategory.setShopId(getCurShop().getId());
    }

    /**
     * 判断名称是否已存在
     *
     * @param name 品牌名称
     * @return 处理结果
     */
    @RequestMapping(value = "/isExist", method = RequestMethod.GET)
    @ResponseBody
    public ResponseData isExist(@RequestParam(defaultValue = "") String parentId, @RequestParam String name){
        ResponseData responseData = new ResponseData();
        ProductCategory category = this.service.findOne(getCurShop().getId(), name);
        boolean isExist = false;
        if(null != category && category.getStatus() != Constants.STATUS_DELETED){
            isExist = true;

            if(StringUtils.hasText(parentId)){
                isExist = parentId.equalsIgnoreCase(category.getParentId());
            }
        }
        responseData.put("isExist", isExist);
        return responseData;
    }
}
