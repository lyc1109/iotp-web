/*
 * Copyright (c) 2016 - 广东小哈科技股份有限公司 
 * All rights reserved.
 *
 * Created on 2017-05-09
 */
package io.iotp.web.controller.shop;

import com.xiaochenghudong.core.account.entity.User;
import io.iotp.core.user.dto.UserDto;
import io.iotp.core.user.service.UserService;
import io.iotp.module.shop.entity.Shop;
import io.iotp.module.shop.entity.ShopEmployee;
import io.iotp.module.shop.entity.ShopVendor;
import io.iotp.shop.ShopForm;
import io.iotp.shop.dto.ShopDto;
import io.iotp.shop.dto.ShopVendorDto;
import io.iotp.shop.service.ShopEmployeeService;
import io.iotp.shop.service.ShopService;
import io.iotp.shop.service.ShopVendorService;
import io.iotp.web.controller.IotpCrudController;
import io.springbootstrap.core.annotation.UserLogConfig;
import io.springbootstrap.core.annotation.UserLogable;
import io.springbootstrap.core.api.ResponseData;
import io.springbootstrap.core.exception.ErrorCode;
import io.springbootstrap.core.exception.ServiceException;
import io.springbootstrap.core.util.CollectionUtils;
import io.springbootstrap.core.util.IdConfuseUtils;
import io.springbootstrap.core.util.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 服务商管理Controller
 *
 * @author huchiwei
 * @since 2.0.0
 */
@Controller
@RequestMapping("/shop/vendor")
@UserLogConfig(moduleName = "服务商管理")
public class ShopVendorController extends IotpCrudController<ShopVendor, ShopVendorDto, ShopVendorService>{

    @Autowired
    private ShopService shopService;

    @Autowired
    private UserService userService;

    @Autowired
    private ShopEmployeeService shopEmployeeService;

    @Override
    public Page<ShopVendorDto> getPageData(Pageable pageable, String searchPhrase) {
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.ASC, "status"));
        orders.add(new Sort.Order(Sort.Direction.DESC, "createdAt"));
        Pageable newPageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), new Sort(orders));

        int[] status = new int[]{ShopVendor.ST_NORMAL, ShopVendor.ST_PROCESSING, ShopVendor.ST_TERMINATION, ShopVendor.ST_REJECT};
        Page<ShopVendor> page = this.service.getPage(newPageable, getCurShop().getId(), status, searchPhrase);
        return this.mapToDtoPage(pageable, page);
    }

    /**
     * 审核通过
     *
     * @return 提交结果
     */
    @RequestMapping(value = "/{entityId}/process", method = RequestMethod.POST, headers="X-Requested-With")
    @ResponseBody
    @UserLogable(methodName = "服务商申请审核通过", description = "'服务商id：' + #entityId")
    public ResponseData process(@PathVariable String entityId){
        ResponseData responseData = new ResponseData();
        this.service.process(IdConfuseUtils.decodeId(entityId), getCurUser(), "审核通过");
        return responseData;
    }

    /**
     * 审核拒绝
     *
     * @return 提交结果
     */
    @RequestMapping(value = "/{entityId}/reject", method = RequestMethod.POST, headers="X-Requested-With")
    @ResponseBody
    @UserLogable(methodName = "服务商申请审核拒绝", description = "'服务商id：' + #entityId")
    public ResponseData reject(@PathVariable String entityId, @RequestParam(defaultValue = "") String memos){
        ResponseData responseData = new ResponseData();
        this.service.reject(IdConfuseUtils.decodeId(entityId), getCurUser(), memos);
        return responseData;
    }

    /**
     * 服务授权
     *
     * @return 提交结果
     */
    @RequestMapping(value = "/{entityId}/authService", method = RequestMethod.POST, headers="X-Requested-With")
    @ResponseBody
    @UserLogable(methodName = "服务商服务授权", description = "'服务商id：' + #entityId")
    public ResponseData authService(@PathVariable String entityId){
        ResponseData responseData = new ResponseData();

        String[] shopServiceConfuseIds = this.getStringArrayParameter("shopServiceIds", null, ",");
        String shopServiceIds = "";
        if (null != shopServiceConfuseIds && shopServiceConfuseIds.length > 0) {
            for (String shopServiceConfuseId : shopServiceConfuseIds) {
                if(shopServiceIds != "")
                    shopServiceIds += ",";
                shopServiceIds += IdConfuseUtils.decodeId(shopServiceConfuseId);
            }
        }
        this.service.authShopService(IdConfuseUtils.decodeId(entityId), shopServiceIds);
        return responseData;
    }

    /**
     * 服务商解约
     *
     * @return 提交结果
     */
    @RequestMapping(value = "/{entityId}/termination", method = RequestMethod.POST, headers="X-Requested-With")
    @ResponseBody
    @UserLogable(methodName = "服务商解约", description = "'服务商id：' + #entityId")
    public ResponseData termination(@PathVariable String entityId, @RequestParam(defaultValue = "") String memos){
        ResponseData responseData = new ResponseData();
        this.service.termination(IdConfuseUtils.decodeId(entityId), memos);
        return responseData;
    }

    /**
     * 恢复合作
     *
     * @return 提交结果
     */
    @RequestMapping(value = "/{entityId}/untermination", method = RequestMethod.POST, headers="X-Requested-With")
    @ResponseBody
    @UserLogable(methodName = "恢复服务商合作", description = "'服务商id：' + #entityId")
    public ResponseData untermination(@PathVariable String entityId){
        ResponseData responseData = new ResponseData();
        this.service.untermination(IdConfuseUtils.decodeId(entityId));
        return responseData;
    }

    /**
     * 选择服务商对话框
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public String selectServiceVendorDlg(Model model){
        String shopServiceId = this.getParameter("shopServiceId", "");
        model.addAttribute("shopServiceId", shopServiceId);
        return this.viewPath("/selectServiceVendorDlg::selectShopVendorGrid");
    }

    /**
     * 获取选择服务商对话框
     *
     * @param pageable
     * @return
     */
    @RequestMapping(value = "/select/page", method = RequestMethod.GET, headers="X-Requested-With")
    @ResponseBody
    public ResponseData getSelectPage(Pageable pageable){
        // 根据当前店铺获取相关服务商
        String shopServiceId = this.getParameter("shopServiceId", "");
        if(StringUtils.hasText(shopServiceId) && !Objects.equals(shopServiceId, "null")){
            shopServiceId = String.valueOf(IdConfuseUtils.decodeId(shopServiceId));
        } else{
            shopServiceId = null;
        }
        Page<Shop> shopPage = this.service.findShopVendor(pageable, getCurShop().getId(), shopServiceId);
        Page<ShopDto> page =this.entityDtoMapService.mapToDtoPage(pageable, shopPage, ShopDto.class);
        ResponseData responseData = new ResponseData();
        if(null != page && page.hasContent()){
            responseData.put("current", (pageable.getPageNumber()+1));
            responseData.put("rowCount", pageable.getPageSize());
            responseData.put("total", page.getTotalElements());
            responseData.put("rows", page.getContent());
        }else{
            responseData = this.blankPageData();
        }
        return responseData;
    }

    // ============================================================================
    // 服务商申请注册相关方法 ==========================================================
    /**
     * 服务商申请注册
     *
     * @return 注册地址
     */
    @RequestMapping(value = "/join/{shopId}", method = RequestMethod.GET)
    @UserLogable(methodName = "服务商申请注册", description = "'厂商ID：' + #shopId")
    public String join(Model model, @PathVariable String shopId){
        String shopEntityId = shopId.substring(shopId.indexOf("_") + 1);
        Shop parent = this.shopService.load(IdConfuseUtils.decodeId(shopEntityId));
        model.addAttribute("parentShop", this.entityDtoMapService.mapToDto(parent, ShopDto.class));
        return this.viewPath("/form-join");
    }

    /**
     * 提交服务商申请注册
     *
     * @return 提交结果
     */
    @RequestMapping(value = "/join/{shopId}/save", method = RequestMethod.POST, headers="X-Requested-With")
    @ResponseBody
    @UserLogable(methodName = "提交服务商申请注册", description = "'服务商名称：' + #shopForm.name")
    public ResponseData submitServiceRequest(@ModelAttribute ShopForm shopForm){
        ResponseData responseData = new ResponseData();

        // 用户信息
        String userId = this.getParameter("userId", "");
        User user;
        if (StringUtils.hasText(userId)) {
            user = this.userService.load(IdConfuseUtils.decodeId(userId));
        } else {
            String userMobile = this.getParameter("userMobile", "");
            String userNickname = this.getParameter("userNickname", "");
            String userLoginPwd = this.getParameter("userLoginPwd", "");
            String confirmLoginPwd = this.getParameter("confirmLoginPwd", "");
            if (!userLoginPwd.equals(confirmLoginPwd)) {
                throw new ServiceException("连续2次输入的登录口令不一致");
            }

            user = new User();
            user.setMobile(userMobile);
            user.setLoginName(userMobile);
            user.setName(userNickname);
            user.setNickname(userNickname);

            PasswordEncoder encoder = new BCryptPasswordEncoder();
            user.setLoginPwd(encoder.encode(userLoginPwd));
        }

        // 店铺信息
        String shopId = this.getParameter("shopId", "");
        ShopVendor shopVendor = new ShopVendor();
        if (StringUtils.hasText(shopId)) {
            Shop shop = this.shopService.load(IdConfuseUtils.decodeId(shopId));
            BeanUtils.copyProperties(shop, shopVendor, "id");
            shopVendor.setLogoImageId(shop.getLogoImageId());
            shopVendor.setShopId(shop.getId());
        } else {
            BeanUtils.copyProperties(shopForm, shopVendor, "id");
            shopVendor.setLogoImageId(IdConfuseUtils.decodeId(shopForm.getLogoImageId()));

            String businessLicenseImgConfuseId = this.getParameter("businessLicenseImgConfuseId", "");
            if(StringUtils.hasText(businessLicenseImgConfuseId))
                shopVendor.setBusinessLicenseImgId(IdConfuseUtils.decodeId(businessLicenseImgConfuseId));
        }
        shopVendor.setShopFactoryId(IdConfuseUtils.decodeId(shopForm.getManufacturerId()));
        shopVendor.setCreatedAt(new Date());
        shopVendor.setStatus(ShopVendor.ST_PROCESSING);

        shopVendor = this.service.register(user, shopVendor);
        responseData.put("id", shopVendor.getId());
        responseData.put("name", shopVendor.getName());
        return responseData;
    }

    /**
     * 根据手机号码查询用户是否已注册服务商
     *
     * @return 提交结果
     */
    @RequestMapping(value = "/find/exist", method = RequestMethod.GET, headers="X-Requested-With")
    @ResponseBody
    public ResponseData findExistShopVendor(@RequestParam String mobile, @RequestParam String factoryShopId){
        ResponseData responseData = new ResponseData();

        // 查询是否已存在用户信息
        User user = this.userService.findByMobile(mobile);
        if (null == user) {
            responseData.put("isExist", false);
            return responseData;
        }
        responseData.put("user", this.entityDtoMapService.mapToDto(user, UserDto.class));

        // 查询是否已存在员工信息
        List<ShopEmployee> shopEmployees = this.shopEmployeeService.findAllByUserId(user.getId());
        if (CollectionUtils.isEmpty(shopEmployees)) {
            responseData.put("isExist", false);
            return responseData;
        }

        // 查询该员工所在的店铺信息
        List<Shop> shops = new ArrayList<>();
        for (ShopEmployee shopEmployee : shopEmployees) {
            Shop shop = this.shopService.load(shopEmployee.getShopId());
            if(null != shop)
                shops.add(shop);
        }

        // 查询厂商下的服务商信息
        List<ShopVendor> shopVendors = this.service.findAll(IdConfuseUtils.decodeId(factoryShopId));
        if (CollectionUtils.isNotEmpty(shopVendors)) {
            for(Iterator<Shop> it = shops.iterator(); it.hasNext();) {
                Shop shop = it.next();
                for (ShopVendor shopVendor : shopVendors) {
                    // 如果员工所在店铺已在厂商的服务商里面，则移除不可选择
                    if (Objects.equals(shop.getId(), shopVendor.getShopId())) {
                        it.remove();
                    }
                }
            }
        }

        responseData.put("isExist", CollectionUtils.isNotEmpty(shops));
        responseData.put("shops", this.entityDtoMapService.mapToDtos(shops, ShopDto.class));
        return responseData;
    }

    /**
     * 服务商申请注册查询
     *
     * @return 申请查询地址
     */
    @RequestMapping(value = "/join/{shopId}/query", method = RequestMethod.GET)
    public String joinQuery(Model model, @PathVariable String shopId){
        String shopEntityId = shopId.substring(shopId.indexOf("_") + 1);
        Shop parent = this.shopService.load(IdConfuseUtils.decodeId(shopEntityId));
        model.addAttribute("parentShop", this.entityDtoMapService.mapToDto(parent, ShopDto.class));
        return this.viewPath("/form-join-query");
    }

    /**
     * 查询服务商申请注册审核结果
     *
     * @return 提交结果
     */
    @RequestMapping(value = "/join/{shopId}/query", method = RequestMethod.POST, headers="X-Requested-With")
    @ResponseBody
    @UserLogable(methodName = "查询服务商申请注册审核结果", description = "'手机号码：' + #mobile")
    public ResponseData joinQuery(@PathVariable String shopId, @RequestParam String mobile){
        ResponseData responseData = new ResponseData();
        List<ShopVendor> shopVendors = this.service.findAll(IdConfuseUtils.decodeId(shopId), mobile);
        responseData.put("shopVendors", this.mapToDtos(shopVendors));
        return responseData;
    }


    @RequestMapping(value = "/join/{shopFactoryId}/isExist",method = RequestMethod.GET,headers = "X-Requested-With")
    @ResponseBody
    @UserLogable(methodName = "服务名唯一校验")
    public ResponseData isExistShopName(@RequestParam(defaultValue = "") String name,@PathVariable String shopFactoryId){
        ResponseData responseData = new ResponseData();
        if(StringUtils.hasText(name)){
            if(this.service.isExistShopVendor(IdConfuseUtils.decodeId(shopFactoryId),name.trim())){
                responseData = new ResponseData(ErrorCode.ENTITY_NOT_UNIQUE);
            }
        }
        return responseData;
    }
}
