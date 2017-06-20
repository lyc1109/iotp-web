/**
 * Copyright (c) 2015 - Two Steps From Java Group.
 * All rights reserved.
 *
 * Created on 2017-03-01
 */
package io.iotp.web.controller;

import io.springbootstrap.core.dto.IdDto;
import io.springbootstrap.core.entity.IdEntity;
import io.springbootstrap.core.service.SimpleService;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 对Iot Platform基Controller进一步扩展，实现Service的自动织入
 *
 * @author CD826
 * @since 2.0.0
 */
abstract public class IotpCrudController<E extends IdEntity, D extends IdDto, S extends SimpleService<E>> extends IotpBaseController<E, D> {
    @Autowired
    protected S service;

    @Override
    public SimpleService<E> getSimpleService() {
        return service;
    }
}
