/**
 * Copyright (c) 2015 - 广州小橙信息科技有限公司
 * All rights reserved.
 *
 * Created on 2016-11-02
 */
package io.iotp.web.controller.shop;

import com.xiaochenghudong.core.util.Constants;
import io.iotp.core.security.IotpSessionContext;
import io.iotp.module.shop.entity.ShopEmployee;
import io.iotp.shop.dto.ShopDto;
import io.iotp.shop.dto.ShopEmployeeDto;
import io.iotp.shop.service.ShopEmployeeService;
import io.iotp.shop.service.ShopService;
import io.iotp.module.shop.entity.Shop;
import io.iotp.shop.service.ShopVendorService;
import io.iotp.web.controller.IotpCrudController;
import io.springbootstrap.core.annotation.UserLogConfig;
import io.springbootstrap.core.api.ResponseData;
import io.springbootstrap.core.util.IdConfuseUtils;
import io.springbootstrap.core.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 店铺管理Controller
 *
 * @author CD826
 * @since 1.0.0
 */
@Controller
@RequestMapping("/shop/shop")
@UserLogConfig(moduleName = "店铺管理")
public class ShopController extends IotpCrudController<Shop, ShopDto, ShopService> {

    @Autowired
    private ShopEmployeeService shopEmployeeService;

    @Autowired
    private ShopVendorService shopVendorService;

    /**
     * 列表页面
     *
     * @return 页面路径
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public String list(){
        return this.viewPath("/list");
    }

    @Override
    public Page<ShopDto> getPageData(Pageable pageable, String searchPhrase) {
        return null;
    }

    /**
     * 选择服务人员对话框
     * @param model
     * @return
     */
    @RequestMapping(value = "/selectServiceMan", method = RequestMethod.GET)
    public String selectServiceManDlg(Model model){
        // 根据当前店铺获取相关服务商
        Shop shop = IotpSessionContext.getShop();
        List<ShopEmployee> shopEmployees = null;
        if(null != shop){
            shopEmployees = this.shopEmployeeService.findServiceMans(shop.getId());
        }
        model.addAttribute("serviceMans", this.entityDtoMapService.mapToDtos(shopEmployees, ShopEmployeeDto.class));
        return this.viewPath("/selectServiceManDlg");
    }

    /**
     * 获取店铺列表
     * @param shopType
     * @param searchPhrase
     * @return
     */
    @RequestMapping(value = "/shopList", method = RequestMethod.GET)
    @ResponseBody
    public ResponseData getVendorShops(@RequestParam String shopType, @RequestParam String searchPhrase){
        int page = this.getIntParameter("page", 0);
        Pageable pageable = new PageRequest(page, 20, new Sort(new Sort.Order(Sort.Direction.ASC, "name")));
        Page<Shop> shops = this.service.getPage(pageable, shopType, new int[]{Constants.STATUS_ENABLED}, null, searchPhrase);
        return this.toBootGrid(pageable, this.mapToDtoPage(pageable, shops));
    }

    /**
     * 检测申请的店铺名称是否唯一
     *
     * @return 提交结果
     */
    @RequestMapping(value = "/isExist", method = RequestMethod.GET, headers="X-Requested-With")
    @ResponseBody
    public ResponseData findExistShopVendor(@RequestParam String shopName){
        ResponseData responseData = new ResponseData();
        responseData.put("isExist", this.service.isExistName(shopName));
        return responseData;
    }
}
