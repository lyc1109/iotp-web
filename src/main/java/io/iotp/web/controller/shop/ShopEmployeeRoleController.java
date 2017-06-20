/**
 * Copyright (c) 2015 - 广州小橙信息科技有限公司
 * All rights reserved.
 *
 * Created on 2016-11-18
 */
package io.iotp.web.controller.shop;

import io.iotp.module.shop.entity.ShopEmployeeRole;
import io.iotp.shop.dto.ShopEmployeeRoleDto;
import io.iotp.shop.service.ShopService;
import io.iotp.web.controller.IotpBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


/**
 * 店铺员工角色管理Controller
 *
 * @author CD826
 * @since 1.0.0
 */
@Controller
@RequestMapping("/shop/employee/role")
public class ShopEmployeeRoleController extends IotpBaseController<ShopEmployeeRole, ShopEmployeeRoleDto> {
    @Autowired
    private ShopService shopService;

    // ========================================================================
    // Page/List Methods ======================================================
    @Override
    public Page<ShopEmployeeRoleDto> getPageData(Pageable pageable, String searchPhrase) {
        return this.shopService.getEmployeeRolePage(pageable, this.getCurShop().getId(), searchPhrase);
    }
}
