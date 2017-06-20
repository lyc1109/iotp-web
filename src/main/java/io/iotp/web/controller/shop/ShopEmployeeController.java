/**
 * Copyright (c) 2015 - 广州小橙信息科技有限公司
 * All rights reserved.
 *
 * Created on 2016-11-18
 */
package io.iotp.web.controller.shop;

import com.xiaochenghudong.core.account.entity.User;
import com.xiaochenghudong.core.util.Constants;
import io.iotp.core.security.IotpSessionContext;
import io.iotp.core.user.dto.UserDto;
import io.iotp.core.user.service.UserService;
import io.iotp.module.shop.entity.Shop;
import io.iotp.module.shop.entity.ShopEmployee;
import io.iotp.shop.dto.ShopEmployeeDto;
import io.iotp.shop.service.ShopEmployeeService;
import io.iotp.web.controller.IotpCrudController;
import io.springbootstrap.core.annotation.UserLogConfig;
import io.springbootstrap.core.annotation.UserLogable;
import io.springbootstrap.core.api.ResponseData;
import io.springbootstrap.core.util.IdConfuseUtils;
import io.springbootstrap.core.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * 店铺员工管理Controller
 *
 * @author CD826
 * @since 1.0.0
 */
@Controller
@RequestMapping("/shop/employee")
@UserLogConfig(moduleName = "店铺员工管理")
public class ShopEmployeeController extends IotpCrudController<ShopEmployee, ShopEmployeeDto, ShopEmployeeService> {
    @Autowired
    private UserService userService;

    // ========================================================================
    // Page/List Methods ======================================================
    @Override
    protected void onList() {
        this.forwardParameters("employeeRole", "searchPhrase");
    }

    @Override
    public Page<ShopEmployeeDto> getPageData(Pageable pageable, String searchPhrase) {
        Shop shop = IotpSessionContext.getShop();
        if(null == shop) {
            return null;
        }

        // 组合查询条件
        Map<String, Object> searchFields = new HashMap<>();
        String employeeRole = this.getParameter("employeeRole", "");
        if (StringUtils.hasText(employeeRole)) {
            searchFields.put("employeeRole", employeeRole);
        }
        Page<ShopEmployee> page = this.service.page(pageable, shop.getId(), this.getCfgStatus(), searchFields, searchPhrase);

        return this.mapToDtoPage(pageable, page);
    }

    @Override
    protected void onOpenCreateForm(Model model, ShopEmployeeDto dto) {
        super.onOpenCreateForm(model, dto);

        dto.setUserDto(new UserDto());
        /*dto.setEmployeeId("Employee_" + new Date().getTime());*/
        dto.setEmployeeRole(ShopEmployee.ROLE_CUSTOM_SERVICER);
    }


    @Override
    protected ShopEmployee saveEntity(ShopEmployee entity) {
        ShopEmployee existEmployee = entity;

        // 设置用户信息
        User user = this.userService.findOne(entity.getMobile());
        if(null == user){
            user = new User();
            user.setMobile(entity.getMobile());
            user.setName(entity.getName());
            user.setNickname(entity.getName());
            user.setLoginName(entity.getMobile());
            user.setEmail(entity.getEmail());
            user.setSex(entity.getSex());
            user.setUserNo("User_" + new Date().getTime());
        }else{
            user.setName(entity.getName());
        }
        this.userService.save(user);

        // 设置员工信息
        if(!entity.isNew()){
            existEmployee = this.service.load(entity.getId());
            existEmployee.setEmail(entity.getEmail());
            existEmployee.setEmployeeId(entity.getEmployeeId());
            existEmployee.setName(entity.getName());
            existEmployee.setEmployeeRole(entity.getEmployeeRole());
            if(StringUtils.isNullString(existEmployee.getMobile()) && StringUtils.hasText(entity.getMobile()))
                existEmployee.setMobile(entity.getMobile());
        }else{
            existEmployee.setShopId(getCurShop().getId());
            existEmployee.setStatus(Constants.STATUS_ENABLED);
            existEmployee.setUser(user);
            existEmployee.setSex(Constants.SEX_MAN);
        }
        return this.service.save(existEmployee);
    }

    /**
     * 根据手机号码判断员工信息是否已存在
     *
     * @param mobile 员工手机号码
     * @param mobile 员工手机号码
     * @return ResponseData请求结果
     */
    @RequestMapping(value = "/isExist", method = RequestMethod.GET)
    @ResponseBody
    public ResponseData isExist(@RequestParam(defaultValue = "-1") String id , @RequestParam String mobile){
        ResponseData responseData = new ResponseData();

        boolean isNew = id.equalsIgnoreCase("-1");
        ShopEmployee shopEmployee = null;
        if(!isNew)
            shopEmployee = this.service.load(IdConfuseUtils.decodeId(id));

        // 判断员工手机号码是否已存在
        boolean isExistMobile;
        ShopEmployee existShopEmployee = this.service.find(getCurShop().getId(), mobile);
        if(isNew){
            isExistMobile = null != existShopEmployee;
        }else{
            isExistMobile = null != existShopEmployee && !existShopEmployee.getId().equals(shopEmployee.getId());
        }
        if(isExistMobile && existShopEmployee.getStatus() != Constants.STATUS_DELETED){
            responseData.put("isExist", true);
            responseData.put("errorMsg", "手机号码为：" + mobile + " 的员工信息已存在，不可重复创建");
            return responseData;
        }

        responseData.put("isExist", false);
        return responseData;
    }


    /**
     * 启用员工
     *
     * @param confuseId 员工id
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
     * 禁用员工
     *
     * @param confuseId 员工id
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
     * 弹出选择服务人员对话框
     *
     * @param model model
     * @return 对话框路径
     */
    @RequestMapping(value = "/select", method = RequestMethod.GET, headers="X-Requested-With")
    public String selectDlg(Model model){
        return this.viewPath("/selectDlg::employee4selectGrid");
    }
}
