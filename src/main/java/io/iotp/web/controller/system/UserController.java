/*
 * Copyright (c) 2016 - 广东小哈科技股份有限公司 
 * All rights reserved.
 *
 * Created on 2017-03-28
 */
package io.iotp.web.controller.system;

import com.xiaochenghudong.core.account.entity.User;
import com.xiaochenghudong.core.system.sms.service.SmsService;
import io.iotp.core.user.dto.UserDto;
import io.iotp.core.user.service.UserService;
import io.iotp.module.shop.entity.Shop;
import io.iotp.module.shop.entity.ShopEmployee;
import io.iotp.shop.dto.ShopDto;
import io.iotp.shop.service.ShopEmployeeService;
import io.iotp.shop.service.ShopService;
import io.iotp.web.controller.IotpBaseController;
import io.springbootstrap.core.annotation.UserLogConfig;
import io.springbootstrap.core.annotation.UserLogable;
import io.springbootstrap.core.api.ResponseData;
import io.springbootstrap.core.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户Controller
 *
 * @author huchiwei
 * @since 2.0.0
 */
@Controller
@RequestMapping("/user")
@UserLogConfig(moduleName = "服务工单管理")
public class UserController extends IotpBaseController<User, UserDto>{

    @Autowired
    private UserService userService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private ShopEmployeeService shopEmployeeService;

    @Override
    public Page<UserDto> getPageData(Pageable pageable, String searchPhrase) {
        return null;
    }

    /**
     * 转到个人信息页面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public String profile(Model model){
        model.addAttribute("curUser", this.mapToDto(getCurUser()));

        List<ShopEmployee> employees = this.shopEmployeeService.findAllByUserId(getCurUser().getId());
        List<Shop> shops = new ArrayList<>();
        for(ShopEmployee shopEmployee : employees){
            Shop shop = this.shopService.load(shopEmployee.getShopId());
            if(null != shop)
                shops.add(shop);
        }
        model.addAttribute("shops", this.entityDtoMapService.mapToDtos(shops, ShopDto.class));

        return this.viewPath("/profile");
    }

    /**
     * 转到重置密码页面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/password/reset", method = RequestMethod.GET)
    public String resetPassword(Model model){
        model.addAttribute("curUser", this.mapToDto(getCurUser()));
        return this.viewPath("/resetPassword");
    }

    /**
     * 用户重置登录口令
     *
     * @param mobileNo
     * @param smsCode
     * @param loginPwd
     * @return
     */
    @RequestMapping(value="/password/reset", method= RequestMethod.POST)
    @UserLogable(methodName = "重置密码")
    @ResponseBody
    public ResponseData resetPassword(@RequestParam("mobileNo") String mobileNo, @RequestParam("smsCode") String smsCode,
                                      @RequestParam("loginPwd") String loginPwd) {
        ResponseData responseData = new ResponseData();
        try {
            // 首先验证短信验证码是否正确
            if (!this.smsService.isMatchSmsCode(mobileNo, smsCode)) {
                return new ResponseData(ResponseData.ERROR_RETURN_CODE, "短信验证码不正确");
            }

            // 进行修改
            this.userService.resetPassword(mobileNo, loginPwd);
            return responseData;
        } catch (ServiceException se) {
            return new ResponseData(se.errorCode.getCode(), se.getMessage());
        }
    }

    /**
     * 转到修改密码页面
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/password/change", method = RequestMethod.GET)
    public String changePassword(Model model){
        model.addAttribute("curUser", this.mapToDto(getCurUser()));
        return this.viewPath("/changePassword");
    }

    /**
     * 用户修改登录口令
     * @param oldLoginPwd
     * @param newLoginPwd
     * @return
     */
    @RequestMapping(value="/password/change", method= RequestMethod.POST)
    @UserLogable(methodName = "修改密码")
    @ResponseBody
    public ResponseData changePassword(@RequestParam("oldLoginPwd") String oldLoginPwd, @RequestParam("newLoginPwd") String newLoginPwd) {
        try {
            this.userService.changePassword(getCurUser().getLoginName(), oldLoginPwd, newLoginPwd);
            return new ResponseData();
        } catch (ServiceException se) {
            return new ResponseData(se.errorCode.getCode(), se.getMessage());
        }
    }

    /**
     * 加载用户信息
     *
     * @param mobile 手机号码
     * @return 用户信息
     */
    @RequestMapping(value="/load", method= RequestMethod.GET)
    @ResponseBody
    public ResponseData load(@RequestParam String mobile) {
        ResponseData responseData = new ResponseData();
        User user = this.userService.findOne(mobile);
        if(null == user)
            return new ResponseData(ResponseData.ERROR_RETURN_CODE, "用户信息不存在");

        responseData.put("user", this.mapToDto(user));
        return responseData;
    }
}
