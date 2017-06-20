/*
 * Copyright (c) 2015 - 广州小橙信息科技有限公司 
 * All rights reserved.
 *
 * Created on 2016-12-29
 */
package io.iotp.web.controller.system;

import com.xiaochenghudong.core.account.entity.User;
import io.iotp.core.notifycenter.dto.UserNotifyDto;
import io.iotp.module.shop.entity.Shop;
import io.iotp.web.controller.IotpBaseController;
import io.springbootstrap.core.api.ResponseData;
import io.springbootstrap.core.service.SimpleService;
import io.springbootstrap.core.util.IdConfuseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.twostepsfromjava.notifycenter.entity.UserNotify;
import org.twostepsfromjava.notifycenter.service.NotifyService;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息提醒Controller
 *
 * @author huchiwei
 * @since 1.0.0
 */
@Controller
@RequestMapping("/notify")
public class NotifyController extends IotpBaseController<UserNotify, UserNotifyDto> {
    @Autowired
    private NotifyService notifyService;

    @Override
    public Page<UserNotifyDto> getPageData(Pageable pageable, String searchPhrase) {
        return null;
    }

    /**
     * 获取我的消息分页
     *
     * @return
     */
    @RequestMapping(value = "/listPage", method = RequestMethod.GET)
    public String listPage(Model model){
        int size = this.getIntParameter("size", 30);
        int curPage = this.getIntParameter("page", 0);
        Pageable pageable = new PageRequest(curPage, size, new Sort(Sort.Direction.DESC, "createdAt"));

        Page<UserNotifyDto> dtoPage = new PageImpl<>(new ArrayList<UserNotifyDto>(), pageable, 0);
        String[] userIds = this.buildCurNotifyUserIds();
        if(null != userIds){
            Page<UserNotify> page = this.notifyService.getPage(pageable, userIds);
            if(page.hasContent()){
                List<UserNotifyDto> dtos = this.mapToDtos(page.getContent());
                dtoPage = new PageImpl<>(dtos, pageable, page.getTotalElements());
            }
        }
        model.addAttribute("page", dtoPage);
        return this.viewPath("/listPage::pageContent");
    }

    /**
     * 获取未读消息
     *
     * @return 请求结果ResponseData
     */
    @RequestMapping(value = "/unread", method = RequestMethod.GET, headers = "X-Requested-With")
    @ResponseBody
    public ResponseData getUnRead(){
        String[] userIds = this.buildCurNotifyUserIds();
        if(null == userIds)
            return new ResponseData(99, "无法获取当前用户信息");

        ResponseData responseData = new ResponseData();
        long unread = this.notifyService.getUnreadCount(userIds);
        responseData.put("countUnread", unread);

        /*List<UserNotify> userNotifies = this.notifyService.findTop20Unread(userIds);
        responseData.put("notifies", this.convertToDtos(userNotifies));*/
        return responseData;
    }

    /**
     * 标记全部已读
     *
     * @return 请求结果ResponseData
     */
    @RequestMapping(value = "/{confuseId}/read", method = RequestMethod.POST, headers = "X-Requested-With")
    @ResponseBody
    public ResponseData markRead(@PathVariable String confuseId){
        this.notifyService.read(this.buildCurNotifyUserIds(), new Long[]{IdConfuseUtils.decodeId(confuseId)});
        return new ResponseData();
    }

    /**
     * 标记全部已读
     *
     * @return 请求结果ResponseData
     */
    @RequestMapping(value = "/readAll", method = RequestMethod.POST, headers = "X-Requested-With")
    @ResponseBody
    public ResponseData readAll(){
        String[] userIds = this.buildCurNotifyUserIds();
        if(null == userIds)
            return new ResponseData(99, "无法获取当前用户信息");
        this.notifyService.readAll(this.buildCurNotifyUserIds());
        return new ResponseData();
    }

    protected String[] buildCurNotifyUserIds(){
        Shop curShop = this.getCurShop();
        User curUser = this.getCurUser();
        if(null != curShop && null != curUser) {
            return new String[]{Shop.idWithPrefix(curShop.getId()), User.idWithPrefix(curUser.getId())};
        }
        return null;
    }
}
