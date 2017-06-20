/*
 * Copyright (c) 2016 - 广东小哈科技股份有限公司
 * All rights reserved.
 *
 * Created on 2017-04-12
 */
package io.iotp.web.controller.product;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.iotp.module.product.entity.Product;
import io.iotp.module.product.faq.entity.ProductFaq;
import io.iotp.product.dto.ProductFaqDto;
import io.iotp.product.service.ProductFaqService;
import io.iotp.product.service.ProductService;
import io.iotp.web.controller.IotpCrudController;
import io.springbootstrap.core.annotation.UserLogConfig;
import io.springbootstrap.core.annotation.UserLogable;
import io.springbootstrap.core.api.ResponseData;
import io.springbootstrap.core.exception.ErrorCode;
import io.springbootstrap.core.util.IdConfuseUtils;
import io.springbootstrap.core.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 产品常见问题库Controller
 *
 * @author wuhaohang
 * @since 2.0.0
 */
@Controller
@RequestMapping("/shop/product/productFaq")
@UserLogConfig(moduleName = "产品问题库管理")
public class ProductFaqController extends IotpCrudController<ProductFaq,ProductFaqDto,ProductFaqService> {
    @Autowired
    ProductService productService;




    /**
     * 获取具体的分页数据
     *
     * @param pageable     分页对象
     * @param searchPhrase 搜索条件
     * @return Page对象
     */
    @Override
    public Page<ProductFaqDto> getPageData(Pageable pageable, String searchPhrase) {
        int[] searchStatus = null;
        int status = this.getIntParameter("status", -1);
        if (status > -1) {
            searchStatus = new int[]{status};
        } else {
            searchStatus = this.getCfgStatus();
        }
        Page<ProductFaq> productFaqs = this.service.getPage(pageable,this.getCurShop().getId(),searchStatus,null,searchPhrase);
        return this.mapToDtoPage(pageable,productFaqs);
    }

    /**
     * 保存前对需要保存实体所附加的额外处理动作
     *
     * @param entity 所要保存的实体对象
     */
    @Override
    protected void onSaveEntity(ProductFaq entity) {
        super.onSaveEntity(entity);
        entity.setShopId(this.getCurShop().getId());
        //保存关联产品============
        if(entity.getIs_common_quest()==1){  //    通用问题清空关联产品
            entity.setProducts(null);
            return;
        }
        String[] productId = this.getRequest().getParameterValues("productId");
        if(productId != null && productId.length>0){
            Set<Product> products = Sets.newHashSet();
            for(String pid : productId){
                Product product = new Product();
                product.setId(IdConfuseUtils.decodeId(pid));
                products.add(product);
            }
            entity.setProducts(products);
        }
        //=======================

    }


    /**
     * 打开编辑页面所要附加的处理
     *
     * @param model model对象
     * @param dto   dto对象
     */
    @Override
    protected void onOpenEditForm(Model model, ProductFaqDto dto) {
        super.onOpenEditForm(model, dto);

    }

    /**
     * 打开创建页面所要附加的处理
     *
     * @param model model对象
     * @param dto   dto对象
     */
    @Override
    protected void onOpenCreateForm(Model model, ProductFaqDto dto) {
        super.onOpenCreateForm(model, dto);
        String productId = this.getParameter("productId","");
        if(StringUtils.hasText(productId)){
            Product product = productService.load(IdConfuseUtils.decodeId(productId));
            Map map = Maps.newHashMap();
            map.put("id",product.getConfuseId());
            map.put("productName",product.getName());
            dto.setProductList(Lists.newArrayList(map));
        }
    }

    /**
     * 删除问题
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}/remove",method = RequestMethod.DELETE,headers="X-Requested-With")
    @ResponseBody
    @UserLogable(methodName = "删除")
    public ResponseData removeFaq(@PathVariable String id){
        ResponseData responseData = new ResponseData();
        try {
            ProductFaq productFaq = this.service.load(IdConfuseUtils.decodeId(id));
            if(productFaq == null){
                responseData.setReturnCode(999);
                responseData.setReturnMsg("不存在!");
                return responseData;
            }
            productFaq.setStatus(com.xiaochenghudong.core.util.Constants.STATUS_DELETED);
            this.service.save(productFaq);
        }catch (Exception e){
            responseData = new ResponseData(ErrorCode.BAD_REQUEST);
        }
        return responseData;
    }

    /**
     * 解除关联
     * @param productId
     * @param faqId
     * @return
     */
    @RequestMapping(value = "/{productId}/removefaq",method = RequestMethod.POST,headers = "X-Requested-With")
    @ResponseBody
    @UserLogable(methodName = "解除关联")
    public ResponseData removeFaqRef(@PathVariable String productId,@RequestParam(value = "faqId") String  faqId){
        ResponseData responseData = new ResponseData();
        ProductFaq productFaq = this.service.load(IdConfuseUtils.decodeId(faqId));
        Set<Product> products = productFaq.getProducts();
        Iterator<Product> iterator = products.iterator();
        while(iterator.hasNext()){
            Product product = iterator.next();
            if(product.getId() == IdConfuseUtils.decodeId(productId)){
                iterator.remove();
            }
        }
        try {
            productFaq.setProducts(products);
            this.service.save(productFaq);
        }catch (Exception e){
             responseData = new ResponseData(ErrorCode.AUTH_ERROR);
        }

        return responseData;
    }



    /**
     * 页签列表页面
     *
     * @return 页面路径
     */
    @RequestMapping(value = "list", method = RequestMethod.GET)
    public String list4tab(){
        this.forwardParameters("productId");
        return this.viewPath("/productFaqList");
    }

    /**
     * 获取问题列表
     * @param productId
     * @return
     */
    @RequestMapping(value = "/{productId}/faqPage",method = RequestMethod.GET, headers="X-Requested-With")
    @ResponseBody
    @UserLogable(methodName = "产品问题列表")
    public ResponseData faqPage(Pageable pageable ,@PathVariable String productId){
        Page<ProductFaq> page = this.service.findPage(pageable,getCurShop().getId(),IdConfuseUtils.decodeId(productId),"");
        Page<ProductFaqDto> productFaqDtos = this.mapToDtoPage(pageable,page);
        return this.toBootGrid(pageable,productFaqDtos);
    }

    /**
     * 获取与问题描述相匹配的产品问题列表
     * @param model
     * @param productId
     * @param desc
     * @return
     */
    @RequestMapping(value = "/{productId}/faqList",method = RequestMethod.GET)
    @UserLogable(methodName = "产品问题选择")
    public String faqList(Model model,@PathVariable String productId,@RequestParam(defaultValue = "") String desc){
        List<ProductFaq> productFaqs = this.service.find(this.getCurShop().getId(),IdConfuseUtils.decodeId(productId),desc);
        model.addAttribute("faqs",this.mapToDtos(productFaqs));
        return this.viewPath("/productFaqDlg");
    }

    /**
     * 进入关联问题选择页面
     * @param model
     * @param searchPhrase
     * @return
     */
    @RequestMapping(value = "/{productId}/selectfaqs",method = RequestMethod.GET, headers="X-Requested-With")
    @UserLogable(methodName = "进入关联问题选择页面")
    public String selectfaqs(Model model,@RequestParam(value = "searchPhrase",defaultValue = "") String searchPhrase,@PathVariable String productId){
        /*int[] status = new int[]{Constants.STATUS_ENABLED};
        List<ProductFaq> productFaqs = this.service.findAll(this.getCurShop().getId(),IdConfuseUtils.decodeId(productId),status,searchPhrase);
        model.addAttribute("faqs",this.mapToDtos(productFaqs));
        model.addAttribute("productId",productId);*/
        return this.viewPath("/selectFaqDlg");
    }

    /**
     * 关联问题-获取问题列表
     *
     * @param pageable
     * @param searchPhrase
     * @param productId
     * @return
     */

    @RequestMapping(value = "/{productId}/selectfaqs/list",method = RequestMethod.GET,headers = "X-Requested-With")
    @ResponseBody
    @UserLogable(methodName = "关联问题-获取问题列表")
    public ResponseData selectfaqsList(Pageable pageable,@RequestParam(value = "searchPhrase",defaultValue = "") String searchPhrase,@PathVariable String productId){

        Page<ProductFaq> productFaqs  = this.service.getProductFaqDlg(pageable,this.getCurShop().getId(),new int[]{0},IdConfuseUtils.decodeId(productId),searchPhrase);

        return this.toBootGrid(pageable,this.mapToDtoPage(pageable,productFaqs));
    }



    /**
     * 添加关联问题
     * @param productId
     * @return
     */
    @RequestMapping(value = "/{productId}/addfaqs",method = RequestMethod.POST,headers = "X-Requested-With")
    @ResponseBody
    @UserLogable(methodName = "产品添加关联问题")
    public ResponseData addFaqs(@PathVariable String productId){
        ResponseData responseData = new ResponseData();
        List<String> list = Lists.newArrayList();
        String[] faqs = this.getStringArrayParameter("faqIds",null);
        if(faqs == null || faqs.length == 0){
            return responseData;
        }
        for(String str : faqs){
            if(StringUtils.hasText(str)){
                list.add(str);
            }

        }
        long[] lids = new long[list.size()];
        for(int i=0 ;i < list.size();i++){
                lids[i] = IdConfuseUtils.decodeId(list.get(i));
        }
        this.service.addFags(lids,IdConfuseUtils.decodeId(productId));
        return responseData;
    }
}
