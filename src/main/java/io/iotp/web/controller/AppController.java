/*
 * Copyright (c) 2015 - 广州小橙信息科技有限公司 
 * All rights reserved.
 *
 * Created on 2016-12-01
 */
package io.iotp.web.controller;

import com.xiaochenghudong.core.account.entity.User;
import io.iotp.core.security.IotpSessionContext;
import io.iotp.core.security.IotpUserDetails;
import io.iotp.module.serviceorder.entity.ServiceOrder;
import io.iotp.module.serviceorder.repository.ServiceOrderRepository;
import io.iotp.module.shop.entity.Shop;
import io.iotp.module.shop.entity.ShopEmployee;
import io.iotp.module.workorder.entity.WorkOrder;
import io.iotp.module.workorder.repository.WorkOrderRepository;
import io.iotp.shop.service.ShopEmployeeService;
import io.iotp.shop.service.ShopService;
import io.springbootstrap.core.annotation.UserLogable;
import io.springbootstrap.core.api.ResponseData;
import io.springbootstrap.core.controller.AbstractController;
import io.springbootstrap.core.util.CollectionUtils;
import io.springbootstrap.core.util.DateUtils;
import io.springbootstrap.core.util.IdConfuseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * 后台管理程序Controller入口
 *
 * @author huchiwei
 * @since 1.0.0
 */
@Controller
public class AppController extends AbstractController{
    @Autowired
    private ShopService shopService;
    @Autowired
    private ShopEmployeeService shopEmployeeService;
    @Autowired
    private ServiceOrderRepository serviceOrderRepository;
    @Autowired
    private WorkOrderRepository workOrderRepository;

    /**
     * 转到首页
     *
     * @return
     */
    @RequestMapping(value={"/"}, method= RequestMethod.GET)
    @UserLogable(moduleName = "首页", methodName = "首页")
    public String homePage(Model model) throws Exception {
        User curUser = IotpSessionContext.getCurUser();
        Shop curShop = IotpSessionContext.getShop();
        if(null == curUser || null == curShop)
            throw new Exception("请先登录系统");

        // 判断是否需要进行所属店铺选择
        if (null == curShop && CollectionUtils.isNotEmpty(IotpSessionContext.getShops())) {
            // 转发到店铺选择
            this.setAttribute("shops", IotpSessionContext.getShops());
            return "/selectShop";
        } else {
            if (Shop.ST_SERVICE_VENDOR.equalsIgnoreCase(curShop.getShopType())) {
                // 服务商工单数据统计
                model.addAttribute("statistics", this.getVendorStatistics(curShop.getId()));
            } else {
                // 厂商订单数据统计
                model.addAttribute("statistics", this.getStatistics(curShop.getId()));
            }

            model.addAttribute("userNickname", curUser.getName());
            model.addAttribute("shopName", curShop.getName());
            model.addAttribute("shopType", curShop.getShopType());
            return "home";
        }
    }

    /**
     * 进入指定店铺
     *
     * @param model
     * @param shopConfusedId 店铺的Id
     * @return
     * @throws Exception
     */
    @RequestMapping(value={"/{shopConfusedId}"}, method= RequestMethod.GET)
    @UserLogable(moduleName = "首页", methodName = "首页")
    public String homePage(Model model, @PathVariable String shopConfusedId) throws Exception {
        User curUser = IotpSessionContext.getCurUser();
        long curShopId = IdConfuseUtils.decodeId(shopConfusedId);
        if (curShopId <= 0) {
            throw new Exception("所要登录的店铺信息有误，无法打开");
        }

        // 获取用户所属店铺及店铺员工信息
        Shop shop = this.shopService.load(curShopId);
        if (null == shop) {
            throw new Exception("所要登录的店铺信息有误，无法打开");
        }
        ShopEmployee shopEmployee = this.shopEmployeeService.find(shop.getId(), curUser.getId());
        if (null == shopEmployee) {
            throw new Exception("您现在尚不是店铺:" + shop.getName() + " 的员工，无法打开");
        }

        // 更新用户Session中的信息
        IotpUserDetails userDetails = IotpSessionContext.getCurUserDetails();
        userDetails.setShop(shop);
        userDetails.setShopEmployee(shopEmployee);

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + shopEmployee.getEmployeeRole()));
        userDetails.setAuthorities(authorities);
        this.getRequest().getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

        // 处理相关信息
        if (Shop.ST_SERVICE_VENDOR.equalsIgnoreCase(shop.getShopType())) {
            // 服务商工单数据统计
            model.addAttribute("statistics", this.getVendorStatistics(shop.getId()));
        } else {
            // 厂商订单数据统计
            model.addAttribute("statistics", this.getStatistics(shop.getId()));
        }

        model.addAttribute("userNickname", curUser.getName());
        model.addAttribute("shopName", shop.getName());
        model.addAttribute("shopType", shop.getShopType());
        return "home";
    }

    /**
     * 进入登录页面
     *
     * @return 登录页面路径
     */
    @RequestMapping(value={"/login"}, method= RequestMethod.GET)
    @UserLogable(moduleName = "登录", methodName = "登录")
    public String loginPage() {
        return "login";
    }

    /**
     * 店铺数据看板
     *
     * @return
     */
    @RequestMapping(value = "/shop/dashboard", method= RequestMethod.GET)
    public String indexPage(Model model) {
        return this.viewPath("/index");
    }

    // ==================================================================
    // private methods ==================================================

    /**
     * 获取厂商数据统计
     *
     * @param shopId 当前店铺ID
     * @return 数据统计
     */
    private ResponseData getStatistics(long shopId) {
        ResponseData responseData = new ResponseData();

        // 待执行订单
        responseData.put("todoCount", this.serviceOrderRepository.count(shopId, new int[]{ServiceOrder.SOT_TOBECONFIRMED,
                ServiceOrder.SOT_TOBEASSIGNE}, null));

        // 今日订单
        responseData.put("todayCount", this.serviceOrderRepository.count(shopId, null, new Date[]{new Date(), new Date()}));

        // 本周订单
        responseData.put("weekCount", this.serviceOrderRepository.count(shopId, null, DateUtils.curWeekDates()));

        // 本周交易总额
        responseData.put("weekAmount", this.serviceOrderRepository.countAmount(shopId, new int[]{ServiceOrder.SOT_CLOSED}, DateUtils.curWeekDates()));

        return responseData;
    }

    /**
     * 获取服务商数据统计
     *
     * @param shopId 当前店铺ID
     * @return 数据统计
     */
    private ResponseData getVendorStatistics(long shopId) {
        ResponseData responseData = new ResponseData();
        // 待执行订单
        responseData.put("todoCount", this.workOrderRepository.count(shopId, new int[]{WorkOrder.WOT_TOBECONFIRMED,
                WorkOrder.WOT_REDISPATCH, WorkOrder.WOT_TOBEASSIGNE, WorkOrder.WOT_TOBEACCEPT, WorkOrder.WOT_ASSIGNED}, null));

        // 今日订单
        responseData.put("todayCount", this.workOrderRepository.count(shopId, null, new Date[]{new Date(), new Date()}));

        // 本周订单
        responseData.put("weekCount", this.workOrderRepository.count(shopId, null, DateUtils.curWeekDates()));

        // 本周交易总额
        responseData.put("weekAmount", this.workOrderRepository.countAmount(shopId, new int[]{WorkOrder.WOT_CLOSED}, DateUtils.curWeekDates()));
        return responseData;
    }
}
