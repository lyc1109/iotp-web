/*
 * Copyright (c) 2015 - 广州小橙信息科技有限公司
 * All rights reserved.
 *
 * Created on 2016-10-30
 */
package io.iotp.web.controller.system;

import com.xiaochenghudong.core.account.entity.User;
import com.xiaochenghudong.core.system.media.entity.Media;
import com.xiaochenghudong.core.system.media.entity.MediaGroup;
import io.iotp.core.media.dto.MediaDto;
import io.iotp.core.media.dto.MediaGroupDto;
import io.iotp.core.media.service.MediaService;
import io.iotp.core.security.IotpSessionContext;
import io.iotp.module.shop.entity.Shop;
import io.springbootstrap.core.annotation.UserLogConfig;
import io.springbootstrap.core.annotation.UserLogable;
import io.springbootstrap.core.api.ResponseData;
import io.springbootstrap.core.controller.AbstractController;
import io.springbootstrap.core.exception.ErrorCode;
import io.springbootstrap.core.support.RequestJsonParam;
import io.springbootstrap.core.util.IdConfuseUtils;
import io.springbootstrap.core.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 素材管理Controller
 *
 * @author huchiwei
 * @since 1.0.0
 */
@Controller
@RequestMapping("/media")
@UserLogConfig(moduleName = "素材管理")
public class MediaController extends AbstractController{

    @Autowired
    private MediaService mediaService;

    /**
     * 转到素材管理页面
     * @return 页面路径
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public String mediaIndex(Model model){
        return "redirect:/media/images";
    }

    /**
     * 转到图片管理页面
     * @return 页面路径
     */
    @RequestMapping(value = "/images", method = RequestMethod.GET)
    public String mediaImages(Model model){
        this.getImageGroups(model);
        return this.viewPath("/media-images");
    }

    /**
     * 转到语音管理页面
     * @return 页面路径
     */
    @RequestMapping(value = "/audios", method = RequestMethod.GET)
    public String mediaAudios(Model model){
        return this.viewPath("/media-audios::content");
    }

    /**
     * 转到视频管理页面
     * @return 页面路径
     */
    @RequestMapping(value = "/videos", method = RequestMethod.GET)
    public String mediaVideo(Model model){
        return this.viewPath("/media-videos::content");
    }

    /**
     * 转到视频管理页面
     * @return 页面路径
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    @UserLogable(methodName = "上传")
    public ResponseData save(@RequestParam String group, @RequestParam(defaultValue = "true") boolean managable, @RequestJsonParam("medias") List<Media> medias){
        ResponseData resp = new ResponseData();
        long targetId = this.getLongParameter("targetId",-1);
        String targetType = this.getParameter("targetType","");
        if(null == medias || medias.size() == 0){
            return new ResponseData(ErrorCode.NULL_ERROR);
        }

        Shop curShop = IotpSessionContext.getShop();
        User curUser = IotpSessionContext.getCurUser();
        if(null == curShop || null == curUser){
            logger.error("无法获取当前店铺/用户信息");
            return new ResponseData(ErrorCode.NULL_ERROR);
        }
        List<MediaDto> mediaList = this.mediaService.save(curShop, curUser, medias, group, managable);
        // 素材关联
        if((!managable) && targetId > 0 && StringUtils.hasText(targetType)){
            long[] ids = new long[mediaList.size()];
            for(int i=0;i<ids.length;i++){
                ids[i] = IdConfuseUtils.decodeId(mediaList.get(i).getId());
            }
            this.mediaService.saveMediaMapping(ids,targetId,targetType);
        }

        resp.put("files", mediaList);
        return resp;
    }

    /**
     * 获取图片分页列表
     * @return 页面路径
     */
    @RequestMapping(value = "/images/page", method = RequestMethod.GET)
    public String listImages(Model model, @RequestParam(required = false) String groupId){
        this.getImagesPage(model, groupId);
        return "/media/media-images-list::pageContent";
    }

    /**
     * 选择图片对话框
     * @return 页面路径
     */
    @RequestMapping(value = "/images/picker", method = RequestMethod.GET)
    public String showImagesDlg(Model model){
        this.getImageGroups(model);
        this.forwardParameters("multiple");
        return "/media/image-picker::pickerContent";
    }

    /**
     * 获取图片分页列表
     * @return 页面路径
     */
    @RequestMapping(value = "/images/page4picker", method = RequestMethod.GET)
    public String selectImages(Model model, @RequestParam(required = false) String groupId){
        this.getImagesPage(model, groupId);
        return "/media/media-images-list4picker::pageContent";
    }

    /**
     * 获取图片分页列表
     * @return 页面路径
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public ResponseData delete(){
        String[] mediaIds = this.getStringArrayParameter("mediaIds", null, ",");
        if(null == mediaIds || mediaIds.length == 0)
            return new ResponseData(ResponseData.ERROR_RETURN_CODE, "请选择需要处理的素材");

        this.mediaService.delete(mediaIds);

        return new ResponseData();
    }

    // ==========================================================================
    // group methods ============================================================
    @RequestMapping(value = "/group/create", method = RequestMethod.POST, headers="X-Requested-With")
    @ResponseBody
    @UserLogable(methodName = "新建分组")
    public ResponseData createGroup(@RequestParam String group){
        Shop shop = IotpSessionContext.getShop();
        if(null != shop){
            MediaGroup mediaGroup = new MediaGroup(shop.getId(), group);
            if(this.mediaService.isExistGroup(shop.getId(),group)){
                return  new ResponseData(1121,"操作失败:分组已存在!");

            }
            this.mediaService.saveGroup(mediaGroup);
        } else {
            return new ResponseData(ErrorCode.NULL_ERROR);
        }
        return new ResponseData();
    }

    /**
     * 编辑分组
     *
     * @param id    分组id
     * @param group 分组名称
     * @return 处理结果ResponseData
     */
    @RequestMapping(value = "/group/{id}", method = RequestMethod.PATCH, headers="X-Requested-With")
    @ResponseBody
    @UserLogable(methodName = "编辑分组")
    public ResponseData editGroup(@PathVariable String id, @RequestParam String group){
        MediaGroup mediaGroup = this.mediaService.loadGroup(IdConfuseUtils.decodeId(id));
        if(null != mediaGroup){
            mediaGroup.setName(group);
            this.mediaService.saveGroup(mediaGroup);
        }
        return new ResponseData();
    }

    /**
     * 删除分组
     *
     * @param id    分组id
     * @return 处理结果ResponseData
     */
    @RequestMapping(value = "/group/{id}", method = RequestMethod.DELETE, headers="X-Requested-With")
    @ResponseBody
    @UserLogable(methodName = "编辑分组")
    public ResponseData deleteGroup(@PathVariable String id){

        this.mediaService.deleteGroup(IdConfuseUtils.decodeId(id));

        return new ResponseData();
    }

    /**
     * 删除分组和图片
     *
     * @param id    分组id
     * @return 处理结果ResponseData
     */
    @RequestMapping(value = "/group/media/{id}", method = RequestMethod.DELETE, headers="X-Requested-With")
    @ResponseBody
    @UserLogable(methodName = "编辑分组")
    public ResponseData deleteGroupAndMedia(@PathVariable String id){

        this.mediaService.deleteGroupAndMedia(IdConfuseUtils.decodeId(id));


        return new ResponseData();
    }

    /**
     * 选择分组
     *
     * @return 处理结果ResponseData
     */
    @RequestMapping(value = "/group/select", method = RequestMethod.DELETE, headers="X-Requested-With")
    public String deleteGroup(Model model){
        Shop shop = IotpSessionContext.getShop();
        List<MediaGroupDto> mediaGroups = this.mediaService.findGroups(shop.getId());
        model.addAttribute("groups", mediaGroups);
        return this.viewPath("/groupSelectDlg");
    }

    // ==========================================================================
    // private methods ==========================================================
    private void getImagesPage(Model model, String groupId){
        int size = this.getIntParameter("size", 12);
        int page = this.getIntParameter("page", 0);
        Pageable pageable = new PageRequest(page, size, new Sort(Sort.Direction.DESC, "uploadedAt"));

        Shop curShop = IotpSessionContext.getShop();
        if(null != curShop){
            model.addAttribute("page", this.mediaService.findAllImages(pageable, curShop.getId(), StringUtils.isNotBlank(groupId) ? IdConfuseUtils.decodeId(groupId) : -1));
        }
    }

    private void getImageGroups(Model model){
        Shop curShop = IotpSessionContext.getShop();
        if(null != curShop) {
            MediaGroupDto defaultGroup = new MediaGroupDto();
            defaultGroup.setOwnerId(IdConfuseUtils.encodeId(curShop.getId()));
            defaultGroup.setId(IdConfuseUtils.encodeId(0L));
            defaultGroup.setMediaCount(this.mediaService.countImages(curShop.getId(), 0));
            defaultGroup.setName("全部图片");

            List<MediaGroupDto> newGroups = new ArrayList<>();
            newGroups.add(defaultGroup);

            List<MediaGroupDto> groups = this.mediaService.findGroups(curShop.getId());
            newGroups.addAll(groups);

            model.addAttribute("groups", newGroups);
            model.addAttribute("shopId", curShop.getShopId());
            model.addAttribute("mediaGroupPrefix", "shop_" + curShop.getConfuseId() + "/images");
        }
    }
}
