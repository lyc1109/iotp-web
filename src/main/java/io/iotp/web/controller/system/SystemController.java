/*
 * Copyright (c) 2015 - 广州小橙信息科技有限公司 
 * All rights reserved.
 *
 * Created on 2016-11-11
 */
package io.iotp.web.controller.system;

import com.xiaochenghudong.core.account.entity.User;
import com.xiaochenghudong.core.system.sms.entity.Sms;
import com.xiaochenghudong.core.system.sms.service.SmsService;
import io.iotp.core.security.IotpSessionContext;
import io.iotp.core.user.service.UserService;
import io.iotp.module.shop.entity.Shop;
import io.springbootstrap.core.annotation.UserLogConfig;
import io.springbootstrap.core.annotation.UserLogable;
import io.springbootstrap.core.api.ResponseData;
import io.springbootstrap.core.controller.AbstractController;
import io.springbootstrap.core.util.StringUtils;
import io.springbootstrap.modules.aliyun.oss.ImageUtil;
import io.springbootstrap.modules.aliyun.oss.OssService;
import io.springbootstrap.modules.aliyun.oss.OssSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 系统管理Controller
 *
 * @author huchiwei
 * @since 1.0.0
 */
@Controller
@RequestMapping(value = "/sys")
@UserLogConfig(moduleName = "系统服务")
public class SystemController extends AbstractController {
    @Autowired
    private SmsService smsService;

    @Autowired
    private OssService ossService;

    @Autowired
    private UserService userService;

    /**
     * 短信分页数据获取
     *
     * @return BootGrid的分页对象
     */
    @RequestMapping(value = "/sms/page", method = RequestMethod.GET, headers="X-Requested-With")
    @ResponseBody
    @UserLogable(methodName = "列表分页数据")
    public ResponseData getSmsPage(@PageableDefault(sort = {"sendAt"}, direction = Sort.Direction.DESC) Pageable pageable, @RequestParam(required = false) String searchPhrase){
        Page<Sms> page = this.smsService.getPage(pageable, searchPhrase);
        ResponseData responseData = new ResponseData();
        responseData.put("current", (pageable.getPageNumber()+1));
        responseData.put("rowCount", pageable.getPageSize());
        responseData.put("total", page.getTotalElements());
        responseData.put("rows", page.getContent());
        return responseData;
    }

    /**
     * 发送短信验证码
     *
     * @param mobile    手机号码
     * @param checkUser 是否检查用户信息是否存在
     * @return 发送结果
     */
    @RequestMapping(value = "/sms/validcode", method = RequestMethod.POST)
    @ResponseBody
    @UserLogable(methodName = "发送验证码")
    public ResponseData sendValidCodeSms(@RequestParam String mobile, @RequestParam(defaultValue = "false") boolean checkUser){
        if(checkUser){
            User user = this.userService.findOne(mobile);
            if(null == user){
                return new ResponseData(ResponseData.ERROR_RETURN_CODE, "对不起，该手机号码对应的用户信息不存在。");
            }
        }

        Shop curShop = IotpSessionContext.getShop();
        if (null != curShop){
            return this.smsService.sendSmsCode(curShop.getId(), curShop.getTenantId(), mobile);
        }else{
            return this.smsService.sendSmsCode(mobile);
        }
    }

    /**
     * 检验短信验证码
     * @param mobile 手机号码
     * @param code   短信验证码
     * @return 检验结果
     */
    @RequestMapping(value = "/sms/validcode/check", method = RequestMethod.POST)
    @ResponseBody
    public ResponseData sendValidCodeSms(@RequestParam String mobile, @RequestParam String code){
        ResponseData responseData = new ResponseData();
        responseData.put("isMatch", this.smsService.isMatchSmsCode(mobile, code));
        return responseData;
    }

    /**
     * 获取oss WEB直传签名
     * @return 检验结果
     */
    @RequestMapping(value = "/oss/signature", method = RequestMethod.GET)
    @ResponseBody
    public OssSignature getOssWebUploadSignature() throws Exception {
        return this.ossService.getWebUploadSignature();
    }

    /**
     * 获取oss WEB直传签名
     * @return 检验结果
     */
    @RequestMapping(value = "/user/avatar", method = RequestMethod.GET)
    @ResponseBody
    public ResponseData getLoginUserAvatar(@RequestParam String mobile) throws Exception {
        ResponseData responseData = new ResponseData();
        User user = this.userService.findOne(mobile);
        boolean isExist = false;
        boolean hasPwd = false;
        String userAvatar = "";
        if(null != user){
            if(StringUtils.hasText(user.getAvatar()))
                userAvatar = ImageUtil.convert(user.getAvatar(), 128, 128);
            isExist = true;
            hasPwd = StringUtils.hasText(user.getLoginPwd());
        }
        responseData.put("isExist", isExist);
        responseData.put("userAvatar", userAvatar);
        responseData.put("hasPwd", hasPwd);
        return responseData;
    }
}
