/**
 * Copyright (c) 2015 - Two Steps From Java Group.
 * All rights reserved.
 *
 * Created on 2017-02-08
 */
package io.iotp.web.controller;

import com.xiaochenghudong.core.account.entity.User;
import com.xiaochenghudong.core.util.Constants;
import io.iotp.core.security.IotpSessionContext;
import io.iotp.module.shop.entity.Shop;
import io.springbootstrap.core.controller.CrudController;
import io.springbootstrap.core.dto.IdDto;
import io.springbootstrap.core.entity.IdEntity;
import io.springbootstrap.core.service.SimpleService;


/**
 * Iot Platform基Controller
 *
 * @author CD826
 * @since 1.0.0
 */
abstract public class IotpBaseController<E extends IdEntity, D extends IdDto> extends CrudController<E, D> {
    /**
     * 获取配置信息可查看的状态列表
     * @return
     */
    protected int[] getCfgStatus() {
        return new int[]{Constants.STATUS_ENABLED, Constants.STATUS_DISABLED };
    }

    /**
     * 获取当前用户
     * @return 用户信息
     */
    protected User getCurUser(){
        return IotpSessionContext.getCurUser();
    }

    /**
     * 获取当前用户所访问的店铺
     * @return 店铺信息
     */
    protected Shop getCurShop(){
        return IotpSessionContext.getShop();
    }

    @Override
    public SimpleService<E> getSimpleService() {
        return null;
    }
}
