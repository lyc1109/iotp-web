/**
 * Copyright (c) 2015 - 广州小橙信息科技有限公司
 * All rights reserved.
 *
 * Created on 2016-10-09
 */
package io.iotp.web.service;

import com.xiaochenghudong.core.account.entity.User;
import com.xiaochenghudong.core.account.service.impl.UserRepositoryService;
import com.xiaochenghudong.core.tenant.entity.Tenant;
import com.xiaochenghudong.core.tenant.service.TenantService;
import io.iotp.core.media.service.MediaService;
import io.iotp.core.security.IotpUserDetails;
import io.iotp.module.shop.entity.Shop;
import io.iotp.module.shop.entity.ShopEmployee;
import io.iotp.module.shop.service.ShopRepositoryService;
import io.iotp.shop.service.ShopEmployeeService;
import io.springbootstrap.core.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * SpringSecurity中加载用户详细信息接口的实现
 *
 * @author CD826
 * @since 1.0.0
 */
@Service(value = "userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {
    protected Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private UserRepositoryService userRepositoryService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private ShopRepositoryService shopRepositoryService;

    @Autowired
    private ShopEmployeeService shopEmployeeService;

    @Autowired
    private MediaService mediaService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        this.logger.debug("userName = {}", username);

        User user = this.userRepositoryService.findOneByLoginName(username);
        if (null == user) {
            // 尝试通过手机号码加载
            user = this.userRepositoryService.findOneByMobile(username);
        }

        // 如果没有查找到，抛出异常错误
        if(null == user) {
            this.logger.error("用户名称为: {} 的用户信息在系统中不存在", username);
            throw new UsernameNotFoundException("User not found: " + username);
        }

        // 查找当前员工信息
        List<ShopEmployee> shopEmployees = this.shopEmployeeService.findAllByUserId(user.getId());
        if(CollectionUtils.isEmpty(shopEmployees)){
            this.logger.error("员工 {} 信息不存在，无法登陆。", username);
            throw new UsernameNotFoundException("员工 "+ username +" 信息不存在，无法登陆。");
        }

        // 获取当前用户所属所有店铺信息
        List<Shop> shops = new ArrayList<>();
        Shop curShop = null;
        ShopEmployee shopEmployee = null;
        String role = "";
        for(ShopEmployee employee : shopEmployees){
            if (this.isExist(shops, employee.getShopId()))
                continue;

            Shop shop = this.shopRepositoryService.get(employee.getShopId());
            if (null != shop) {
                shops.add(shop);

                if (null == curShop) {
                    curShop = shop;
                    role = employee.getEmployeeRole();
                    shopEmployee = employee;
                }
            }
        }

        // 无店铺信息
        if (shops.isEmpty()) {
            this.logger.error("用户 {} 未绑定相关店铺信息，无法登录。", username);
            throw new UsernameNotFoundException("用户" + username+ "未绑定相关店铺信息，无法登录。");
        }

        // 如果当前用户属于一个店铺，那么将该店铺设置为当前店铺; 否则用户需要选择进入的店铺
        // FIXME: 完成店铺选择后，将下面的判断语句更改为：shops.size() == 1
        if (shops.size() > 0) {
            // 获取店铺的租户信息
            Tenant tenant = this.tenantService.get(curShop.getTenantId());
            if (null == tenant) {
                this.logger.error("店铺 {} 未绑定相关租户信息，无法登录。", curShop.getName());
                throw new UsernameNotFoundException("店铺 " + curShop.getName()+ " 未绑定相关租户信息，无法登录。");
            }

            // 获取用户的店铺员工信息，并获取用户的角色
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

            IotpUserDetails huDongUserDetails = new IotpUserDetails(user, tenant, shops, curShop, shopEmployee, authorities);
            if(curShop.getLogoImageId() > 0){
                huDongUserDetails.setShopLogo(this.mediaService.getMediaPath(curShop.getLogoImageId()));
            }
            return huDongUserDetails;
        } else {
            return new IotpUserDetails(user, shops);
        }

        /* FIXME: 将权限更新为下面的代码
        // 用户角色
        authorities.add(new SimpleGrantedAuthority("ROLE_" + shopEmployee.getEmployeeRole()));

        // 添加用户权限
        Collection<String> authorityCodes = this.applicationSecurityService.getAuthorityCodes(shopEmployee.getEmployeeRole());
        if (CollectionUtils.isNotEmpty(authorityCodes)) {
            for (String authorityCode : authorityCodes) {
                authorities.add(new SimpleGrantedAuthority(authorityCode));
            }
        }*/
    }

    private boolean isExist(List<Shop> shops, long shopId) {
        if (CollectionUtils.isEmpty(shops))
            return false;

        for (Shop shop : shops) {
            if (shopId == shop.getId())
                return true;
        }
        return false;
    }
}
