/**
 * Copyright (c) 2015 - 广州小橙信息科技有限公司
 * All rights reserved.
 *
 * Created on 2016-11-21
 */
package io.iotp.web.controller.product;

import com.xiaochenghudong.core.util.Constants;
import io.iotp.core.util.DeviceType;
import io.iotp.core.util.IotDeviceType;
import io.iotp.iot.devicemodel.service.IotDeviceModelService;
import io.iotp.module.authcode.entity.ProductAuthCode;
import io.iotp.module.iot.devicemodel.entity.IotDeviceModel;
import io.iotp.product.dto.ProductDto;
import io.iotp.core.security.IotpSessionContext;
import io.iotp.module.shop.entity.Shop;
import io.iotp.core.util.ProductType;
import io.iotp.module.product.entity.*;
import io.iotp.product.dto.ProductSpecDto;
import io.iotp.product.service.ProductBandService;
import io.iotp.product.service.ProductCategoryService;
import io.iotp.product.service.ProductService;
import io.iotp.web.controller.IotpCrudController;
import io.springbootstrap.core.annotation.UserLogConfig;
import io.springbootstrap.core.annotation.UserLogable;
import io.springbootstrap.core.api.ResponseData;
import io.springbootstrap.core.service.EntityDtoMapper;
import io.springbootstrap.core.support.RequestJsonParam;
import io.springbootstrap.core.util.CollectionUtils;
import io.springbootstrap.core.util.IdConfuseUtils;
import io.springbootstrap.core.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;


/**
 * 店铺产品管理Controller
 *
 * @author CD826
 * @since 1.0.0
 */
@Controller
@RequestMapping("/shop/product")
@UserLogConfig(moduleName = "商品管理")
public class ProductController extends IotpCrudController<Product, ProductDto, ProductService> {
    @Autowired
    private ProductBandService productBandService;

    @Autowired
    private ProductCategoryService productCategoryService;

    @Autowired
    private EntityDtoMapper<ProductSpec, ProductSpecDto> productSpecEntityDtoMapper;

    @Autowired
    private IotDeviceModelService iotDeviceModelService;

    // Page/List Methods ======================================================
    @Override
    protected void onList() {
        this.forwardParameters("productType", "productBand", "productCategory", "status", "searchPhrase");

        long shopId = this.getCurShop().getId();
        this.setAttribute("bandOptions", this.productBandService.getBandOptions(shopId));
        this.setAttribute("categoryOptions", this.productCategoryService.getCategoryOptions(shopId));
    }

    @Override
    public Page<ProductDto> getPageData(Pageable pageable, String searchPhrase) {
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.ASC, "productType"));
        orders.add(new Sort.Order(Sort.Direction.DESC, "created"));
        Pageable newPageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), new Sort(orders));

        // 组合查询条件
        Map<String, Object> searchFields = new HashMap<>();
        String productType = this.getParameter("productType", "");
        if (StringUtils.hasText(productType)) {
            searchFields.put("productType", productType);
        }

        long productBandId = IdConfuseUtils.decodeId(this.getParameter("productBand", ""));
        if (productBandId > 0) {
            searchFields.put("productBand.id", productBandId);
        }

        long productCategoryId = IdConfuseUtils.decodeId(this.getParameter("productCategory", ""));
        if (productCategoryId > 0) {
            searchFields.put("productCategory", productCategoryId);
        }

        String deviceType = this.getParameter("deviceType", "");
        if (StringUtils.hasText(deviceType)) {
            searchFields.put("deviceType", deviceType);
        }

        int[] searchStatus = null;
        int status = this.getIntParameter("status", -1);
        if (status > -1) {
            searchStatus = new int[]{status};
        } else {
            searchStatus = this.getCfgStatus();
        }

        Page<Product> page = this.service.getPage(newPageable, this.getCurShop().getId(), searchStatus, searchFields, searchPhrase);
        return this.mapToDtoPage(newPageable, page);
    }

    @Override
    protected void onOpenCreateForm(Model model, ProductDto dto) {
        super.onOpenCreateForm(model, dto);

        String productType = this.getParameter("productType", ProductType.PRODUCT);
        dto.setStatus(Constants.STATUS_ENABLED);
        dto.setProductType(productType);
        this.forwardParameters("parentId", "productType");
    }

    @Override
    protected void onOpenForm(Model model, ProductDto dto) {
        super.onOpenForm(model, dto);

        long shopId = this.getCurShop().getId();
        this.setAttribute("bandOptions", this.productBandService.getBandOptions(shopId));
        this.setAttribute("categoryOptions", this.productCategoryService.getCategoryOptions(shopId));
        this.setAttribute("specGroupOptions", this.service.getSpecGroupOptions(shopId));

        // 智能设备类型
        this.setAttribute("iotDeviceTypes", IotDeviceType.getTypes());

        if(!dto.isNew() && dto.getDeviceType().equalsIgnoreCase(DeviceType.INTELLIGENT)){
            IotDeviceModel iotDeviceModel = this.iotDeviceModelService.findOne(dto.getIotDeviceModel());
            String modelName = "";
            if(null != iotDeviceModel)
                modelName = iotDeviceModel.getModelName();
            model.addAttribute("iotDeviceModelName", modelName);
        }

        // 若是配件判断是否已关联给某个产品
        if (!dto.isNew() && dto.getProductType().equals(ProductType.PART)) {
            model.addAttribute("isMapping", this.service.isMappingProduct(IdConfuseUtils.decodeId(dto.getId())));
        }
    }

    /**
     * 保存商品
     *
     * @param productDto           产品DTO
     * @param redirectAttributes   需要转发的参数
     * @param productSpecs         产品规格信息
     * @return
     */
    @RequestMapping(value = "/{entityId}/save", method = RequestMethod.POST)
    @UserLogable(methodName = "发布")
    public String save(@ModelAttribute ProductDto productDto, RedirectAttributes redirectAttributes, @RequestJsonParam("productSpecs") List<ProductSpec> productSpecs){
        // 首先将DTO对象转换为Entity对象
        Product product = this.mapToEntity(productDto);

        // 产品分类
        String[] categoryConfuseIds = this.getRequest().getParameterValues("productCategoryConfuseIds");
        product.setProductCategory1(null);
        product.setProductCategory2(null);
        product.setProductCategory3(null);
        if (!StringUtils.isEmpty(categoryConfuseIds)) {
            int index = 0;
            for (String confuseId : categoryConfuseIds) {
                long categoryId = IdConfuseUtils.decodeId(confuseId);
                if(categoryId <= 0)
                    continue;

                ProductCategory productCategory = new ProductCategory();
                productCategory.setId(categoryId);
                if (index == 0)
                    product.setProductCategory1(productCategory);
                if (index == 1)
                    product.setProductCategory2(productCategory);
                if (index == 2)
                    product.setProductCategory3(productCategory);
                index++;
            }
        }

        // 产品品牌
        long productBandId = IdConfuseUtils.decodeId(this.getParameter("productBandConfuseId", ""));
        if (productBandId > 0) {
            ProductBand productBand = new ProductBand();
            productBand.setId(productBandId);
            product.setProductBand(productBand);
        } else {
            product.setProductBand(null);
        }

        // 产品规格模板
        long productSpecGroupId = IdConfuseUtils.decodeId(this.getParameter("productSpecGroupConfuseId", ""));
        if (productSpecGroupId > 0) {
            ProductSpecGroup productSpecGroup = new ProductSpecGroup();
            productSpecGroup.setId(productSpecGroupId);
            product.setProductSpecGroup(productSpecGroup);
        } else {
            product.setProductSpecGroup(null);
        }

        // 产品主图
        String coverImageConfuseId= this.getParameter("coverImageConfuseId", "");
        if(StringUtils.hasText(coverImageConfuseId))
            product.setCoverImageId(IdConfuseUtils.decodeId(coverImageConfuseId));

        // 智能设备管理
        if(!product.getProductType().equalsIgnoreCase(ProductType.PRODUCT))
            product.setDeviceType(DeviceType.COMMON);
        if(product.getDeviceType().equalsIgnoreCase(DeviceType.COMMON)){
            product.setIotDeviceModel("");
            product.setIotDeviceType("");
        }

        try {
            boolean isNew = product.isNew();
            product.setShopId(getCurShop().getId());

            // 已存在配件绑定关系
            if(!isNew){
                Product existProduct = this.service.load(product.getId());
                if(null != existProduct && existProduct.hasParts())
                    product.setProductParts(existProduct.getProductParts());
            }

            // 保存产品信息
            product = this.service.save(this.getCurUser(), product, productSpecs);

            // 配件绑定所属产品信息
            if(isNew){
                if (product.getProductType().equalsIgnoreCase(ProductType.PART)) {
                    String parentId = this.getParameter("parentId", "");
                    if (StringUtils.hasText(parentId) && !parentId.equalsIgnoreCase("-1") && IdConfuseUtils.decodeId(parentId) > 0) {
                        this.service.addPart(this.getCurUser(), IdConfuseUtils.decodeId(parentId), new long[]{product.getId()});
                    }
                }
            }

            this.onAfterSaveEntity(product);
        }catch (Exception e){
            logger.error("fail to save entity of post", e);
        }

        // 添加提示信息标示
        redirectAttributes.addFlashAttribute("_preAction", "save");
        return "redirect:" + this.getBasePath() + "/" + product.getConfuseId();
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
     * 加载产品规格信息
     * @return
     */
    @RequestMapping(value = "/loadSpec", method = RequestMethod.GET)
    public String loadSpec(){
        long productId = IdConfuseUtils.decodeId(this.getParameter("productId", ""));
        long specGroupId = IdConfuseUtils.decodeId(this.getParameter("specGroupId", ""));

        this.setAttribute("produceSpecs", this.service.loadProductSpecs(productId, specGroupId));

        return this.viewPath("/productSpec::specBody");
    }

    /**
     * 配件列表页面
     * @param model
     * @param productId
     * @return
     */
    @RequestMapping(value = "/{productId}/parts", method = RequestMethod.GET)
    public String partsList(Model model, @PathVariable String productId){
        model.addAttribute("partParentId", productId);
        return this.viewPath("/partList");
    }

    /**
     * 配件分页数据
     * @return
     */
    @RequestMapping(value = "/{productId}/partPage", method = RequestMethod.GET, headers="X-Requested-With")
    @ResponseBody
    public ResponseData partPage(Pageable pageable, @PathVariable String productId){
        Page<Product> page = this.service.getPartPage(pageable, IdConfuseUtils.decodeId(productId), new int[]{Constants.STATUS_ENABLED}, "");
        return this.toBootGrid(pageable, this.mapToDtoPage(pageable, page));
    }

    /**
     * 选择配件
     * @param model
     * @return
     */
    @RequestMapping(value = "/{productId}/selectParts", method = RequestMethod.GET)
    public String selectPartsDlg(Model model, @PathVariable String productId){
        // 根据当前店铺获取相关服务商
        boolean filter = this.getBooleanParameter("filter", true);
        List<Product> partList = new ArrayList<>();

        List<Product> allPartList = this.service.findAll(getCurShop().getId(), ProductType.PART);
        for(Iterator<Product> it = allPartList.iterator(); it.hasNext();) {
            Product part = it.next();
            if(part.getStatus() != Constants.STATUS_ENABLED)
                it.remove();
        }
        if (CollectionUtils.isNotEmpty(allPartList)) {
            if (filter) {
                Product product = this.service.load(IdConfuseUtils.decodeId(productId));
                if (null != product) {
                    for (Product part : allPartList) {
                        if (!product.isPart(part))
                            partList.add(part);
                    }
                } else {
                    partList.addAll(allPartList);
                }
            } else {
                partList.addAll(allPartList);
            }
        }
        model.addAttribute("partList", this.mapToDtos(partList));
        return this.viewPath("/selectPartDlg");
    }

    /**
     * 获取配件列表（select2下拉选择）
     *
     * @return 请求结果
     */
    @RequestMapping(value = "/parts/list/select2", method = RequestMethod.GET, headers="X-Requested-With")
    @ResponseBody
    public ResponseData list4select2(){
        ResponseData resp = new ResponseData();
        List<Product> parts = this.service.findAll(getCurShop().getId(), ProductType.PART);
        resp.put("parts", this.mapToDtos(parts));
        return resp;
    }

    /**
     * 关联配件
     * @param productId
     * @return
     */
    @RequestMapping(value = "/{productId}/addParts", method = RequestMethod.POST, headers="X-Requested-With")
    @ResponseBody
    public ResponseData addParts(@PathVariable String productId){
        ResponseData responseData = new ResponseData();
        String[] partIds = this.getStringArrayParameter("partIds", null);
        if (null == partIds || partIds.length == 0)
            return responseData;

        long[] ids = new long[partIds.length];
        for(int i = 0; i < partIds.length; i++) {
            ids[i] = IdConfuseUtils.decodeId(partIds[i]);
        }
        this.service.addPart(this.getCurUser(), IdConfuseUtils.decodeId(productId), ids);
        return responseData;
    }

    /**
     * 删除配件
     * @param productId
     * @param partId
     * @return
     */
    @RequestMapping(value = "/{productId}/removePart", method = RequestMethod.POST, headers="X-Requested-With")
    @ResponseBody
    public ResponseData removePart(@PathVariable String productId, @RequestParam String partId){
        ResponseData responseData = new ResponseData();
        this.service.removePart(this.getCurUser(), IdConfuseUtils.decodeId(productId), IdConfuseUtils.decodeId(partId));
        return responseData;
    }

    /**
     * 选择商品
     *
     * @param model model
     * @return 对话框路径
     */
    @RequestMapping(value = "/selectProducts", method = RequestMethod.GET)
    public String selectProductDlg(Model model){
        return this.viewPath("/selectProductDlg::product4selectGrid");
    }

    /**
     * 校验商品货号是否存在
     *
     * @param itemCode       商品货号
     * @param specs   规格型号列表
     * @return
     */
    @RequestMapping(value = "/existItemCode", method = RequestMethod.GET, headers="X-Requested-With")
    @ResponseBody
    public ResponseData existItemCode(@RequestParam(defaultValue = "-1") String productId,
                                      @RequestParam(defaultValue = "") String itemCode,
                                      @RequestJsonParam(value = "specs", required = false) List<ProductSpec> specs){
        ResponseData responseData = new ResponseData();

        // 组合所有itemCode做查询
        List<String> itemCodeList = new ArrayList<>();
        if(StringUtils.hasText(itemCode)){
            itemCodeList.add(itemCode);
        }
        if(CollectionUtils.isNotEmpty(specs)){
            for(ProductSpec spec: specs){
                if(StringUtils.isNullString(spec.getItemCode())){
                    responseData = new ResponseData(99, "请填写规格信息中货号");
                    return responseData;
                }

                if (CollectionUtils.isNotEmpty(itemCodeList) && itemCodeList.contains(spec.getItemCode())) {
                    responseData = new ResponseData(10, "本产品中存在货号重复：" + spec.getItemCode());
                    return responseData;
                }

                itemCodeList.add(spec.getItemCode());
            }
        }

        Shop curShop = this.getCurShop();
        String[] itemCodes = itemCodeList.toArray(new String[itemCodeList.size()]);
        List<Product> existProducts = new ArrayList<>();
        List<ProductSpec> existProductSpecs = new ArrayList<>();
        boolean isExist = false;
        boolean isNew = productId.equalsIgnoreCase("-1");

        // 1. 判断现有产品中是否存在
        // 1.1 新建状态若查询列表不为空，则表示货号已存在
        // 1.2 编辑状态若查询列表不为空，且已存在货号产品与当前编辑产品不一致，则表示货号已存在
        List<Product> products = this.service.findAll(curShop.getId(), itemCodes);
        if(CollectionUtils.isNotEmpty(products)){
            if(!isNew){
                for (Product existProduct : products) {
                    if (!existProduct.getConfuseId().equals(productId)) {
                        isExist = true;

                        if(!existProducts.contains(existProduct))
                            existProducts.add(existProduct);
                    }
                }
            }else{
                isExist = true;
                for (Product existProduct : products) {
                    if (itemCodeList.contains(existProduct.getItemCode())) {
                        if(!existProducts.contains(existProduct))
                            existProducts.add(existProduct);
                    }
                }
            }
        }
        if(isExist){
            responseData.put("isExist", true);
            responseData.put("existProducts", this.mapToDtos(existProducts));
            return responseData;
        }


        // 2. 判断现有产品规格是否已存在
        // 2.1 新建状态若查询列表不为空，则表示货号已存在
        // 2.2 编辑状态若查询列表不为空，且已存在货号产品与当前编辑产品不一致，则表示货号已存在
        List<ProductSpec> productSpecs = this.service.findProductSpec(curShop.getId(), itemCodes);
        if(CollectionUtils.isNotEmpty(productSpecs)){
            if(!isNew){
                for (ProductSpec existProductSpec : productSpecs) {
                    Product parent = existProductSpec.getProduct();
                    if(!parent.getConfuseId().equals(productId)){
                        isExist = true;

                        if(!existProductSpecs.contains(existProductSpec))
                            existProductSpecs.add(existProductSpec);
                    }
                }
            }else{
                isExist = true;
                for (ProductSpec existProductSpec : productSpecs) {
                    if(itemCodeList.contains(existProductSpec.getItemCode())){
                        if(!existProductSpecs.contains(existProductSpec))
                            existProductSpecs.add(existProductSpec);
                    }
                }
            }
        }

        responseData.put("isExist", isExist);
        responseData.put("existProducts", this.mapToDtos(existProducts));
        responseData.put("existProductSpecs", this.productSpecEntityDtoMapper.mapToDtos(existProductSpecs, ProductSpecDto.class));
        return responseData;
    }
}
