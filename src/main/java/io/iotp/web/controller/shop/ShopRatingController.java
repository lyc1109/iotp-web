/*
 * Copyright (c) 2016 - 广东小哈科技股份有限公司 
 * All rights reserved.
 *
 * Created on 2017-04-26
 */
package io.iotp.web.controller.shop;

import io.iotp.module.rating.entity.Rating;
import io.iotp.module.rating.entity.RatingReply;
import io.iotp.module.shop.entity.Shop;
import io.iotp.module.shop.entity.ShopEmployee;
import io.iotp.rating.dto.RatingDto;
import io.iotp.rating.dto.RatingReplyDto;
import io.iotp.rating.mapper.RatingReplyEntityDtoMapper;
import io.iotp.rating.service.RatingService;
import io.iotp.shop.service.ShopEmployeeService;
import io.iotp.shop.service.ShopVendorService;
import io.iotp.web.controller.IotpCrudController;
import io.springbootstrap.core.annotation.UserLogConfig;
import io.springbootstrap.core.annotation.UserLogable;
import io.springbootstrap.core.api.ResponseData;
import io.springbootstrap.core.util.IdConfuseUtils;
import io.springbootstrap.core.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 评价管理Controller
 *
 * @author wuhaohang
 * @since 2.0.0
 */
@Controller
@RequestMapping("/shop/rating")
@UserLogConfig(moduleName = "评价管理")
public class ShopRatingController extends IotpCrudController<Rating,RatingDto,RatingService> {

    @Autowired
    RatingReplyEntityDtoMapper ratingReplyEntityDtoMapper;

    @Autowired
    ShopVendorService shopVendorService;

    @Autowired
    ShopEmployeeService shopEmployeeService;


    /**
     * 获取具体的分页数据
     *
     * @param pageable     分页对象
     * @param searchPhrase 搜索条件
     * @return Page对象
     */
    @Override
    public Page<RatingDto> getPageData(Pageable pageable, String searchPhrase) {
        String viewType = this.getParameter("viewType","10");
        double rating = Double.parseDouble(this.getParameter("rating","0.0")) ;
        Page<Rating> ratings = this.service.getPage(pageable,getCurShop().getId(),rating,viewType,getCurShop().getShopType(),searchPhrase);
        return this.mapToDtoPage(pageable,ratings);
    }

    @Override
    protected void onOpenViewForm(Model model, RatingDto dto) {
        super.onOpenViewForm(model, dto);
        List<RatingReply> ratingReplies = this.service.load(IdConfuseUtils.decodeId(dto.getId())).getRatingReplies();
        model.addAttribute("replyList",ratingReplyEntityDtoMapper.mapToDtos(ratingReplies, RatingReplyDto.class));
        String action = this.getParameter("action","");
        model.addAttribute("action",action);
    }

    @Override
    protected void onList() {
        super.onList();
        long totalRating1 = this.service.totalByRating(1.0,this.getCurShop().getId(),this.getCurShop().getShopType()); //统计评分1.0的评价数
        long totalRating2 = this.service.totalByRating(2.0,this.getCurShop().getId(),this.getCurShop().getShopType()); //统计评分2.0的评价数
        long totalRating3 = this.service.totalByRating(3.0,this.getCurShop().getId(),this.getCurShop().getShopType()); //统计评分3.0的评价数
        long totalRating4 = this.service.totalByRating(4.0,this.getCurShop().getId(),this.getCurShop().getShopType()); //统计评分4.0的评价数
        long totalRating5 = this.service.totalByRating(5.0,this.getCurShop().getId(),this.getCurShop().getShopType()); //统计评分5.0的评价数
        long count10 = this.service.count(this.getCurShop().getId(),0.0,"10",getCurShop().getShopType()); //待回复评价数量
        long count30 = this.service.count(this.getCurShop().getId(),0.0,"30",getCurShop().getShopType());//全部评价数量
        this.getRequest().setAttribute("totalRating1",totalRating1);
        this.getRequest().setAttribute("totalRating2",totalRating2);
        this.getRequest().setAttribute("totalRating3",totalRating3);
        this.getRequest().setAttribute("totalRating4",totalRating4);
        this.getRequest().setAttribute("totalRating5",totalRating5);
        this.getRequest().setAttribute("count10",count10);
        this.getRequest().setAttribute("count30",count30);
        this.getRequest().setAttribute("viewType",this.getParameter("viewType","10"));
        this.getRequest().setAttribute("rating",this.getParameter("rating","0.0"));
        this.getRequest().setAttribute("nav_code","rating");
    }

    @RequestMapping(value = "/reply/{ratingId}",method = RequestMethod.POST,headers = "X-Requested-With")
    @ResponseBody
    @UserLogable(methodName = "回复")
    public ResponseData replyRating(@PathVariable String ratingId, @RequestParam(name = "replyContent") String replyContent){
        ResponseData responseData = new ResponseData();
        if(StringUtils.hasText(replyContent)){
            Rating rating = this.service.load(IdConfuseUtils.decodeId(ratingId));
            List<RatingReply> ratingReplies = rating.getRatingReplies();
            if(ratingReplies == null)
                ratingReplies = new ArrayList();
            RatingReply ratingReply = new RatingReply();
            ratingReply.setShopId(this.getCurShop().getId());
            ratingReply.setAuthorUserId(this.getCurUser().getId());
            ratingReply.setReplyContent(replyContent);
            ratingReply.setRating(rating);
            ratingReplies.add(ratingReply);
            rating.setRatingReplies(ratingReplies);
            rating.setLatest_replied_at(new Date());
            this.service.save(rating);
        }
        return responseData;
    }


    @RequestMapping(value = "/count",method = RequestMethod.GET)
    @UserLogable(methodName = "统计分析")
    public String count(Model model){

        //====统计服务商=======
        Pageable pageable = new PageRequest(0,99);
        List<Shop> shopVendors = shopVendorService.findShopVendor(pageable,this.getCurShop().getId(),null).getContent();
        List<Map> shopVendorList = new ArrayList<>();
        for(Shop shopVendor : shopVendors){
            Map map = new HashMap();
            map.put("shopVendorName",shopVendor.getName());
            map.put("rating5",this.service.countByServiceVendorId(shopVendor.getId(),new double[]{5.0}));
            map.put("rating4",this.service.countByServiceVendorId(shopVendor.getId(),new double[]{4.0}));
            map.put("rating3",this.service.countByServiceVendorId(shopVendor.getId(),new double[]{3.0}));
            map.put("rating2",this.service.countByServiceVendorId(shopVendor.getId(),new double[]{2.0}));
            map.put("rating1",this.service.countByServiceVendorId(shopVendor.getId(),new double[]{1.0}));
            map.put("ratingAver",this.service.countServiceVendorRating(shopVendor.getId()));
            shopVendorList.add(map);
        }
        model.addAttribute("shopVendorList",shopVendorList);
        //========================================

        //====统计服务人员=========
        if("FWS".equals(this.getCurShop().getShopType())){
            List<ShopEmployee> shopEmployees = shopEmployeeService.findServiceMans(this.getCurShop().getId());
            List<Map> serviceManList = new ArrayList<>();
            for(ShopEmployee shopEmployee : shopEmployees){
                Map map = new HashMap();
                map.put("serviceManName",shopEmployee.getName());
                map.put("rating5",this.service.countByServiceManIdAndRating(this.getCurShop().getId(),shopEmployee.getUser().getId(),new double[]{5.0}));
                map.put("rating4",this.service.countByServiceManIdAndRating(this.getCurShop().getId(),shopEmployee.getUser().getId(),new double[]{4.0}));
                map.put("rating3",this.service.countByServiceManIdAndRating(this.getCurShop().getId(),shopEmployee.getUser().getId(),new double[]{3.0}));
                map.put("rating2",this.service.countByServiceManIdAndRating(this.getCurShop().getId(),shopEmployee.getUser().getId(),new double[]{2.0}));
                map.put("rating1",this.service.countByServiceManIdAndRating(this.getCurShop().getId(),shopEmployee.getUser().getId(),new double[]{1.0}));
                map.put("ratingAver",this.service.countServiceManRating(this.getCurShop().getId(),shopEmployee.getUser().getId()));

//                map.put("rating5p",this.service.percentServiceMan(this.getCurShop().getId(),shopEmployee.getUser().getId(),5.0));
//                map.put("rating4p",this.service.percentServiceMan(this.getCurShop().getId(),shopEmployee.getUser().getId(),4.0));
//                map.put("rating3p",this.service.percentServiceMan(this.getCurShop().getId(),shopEmployee.getUser().getId(),3.0));
//                map.put("rating2p",this.service.percentServiceMan(this.getCurShop().getId(),shopEmployee.getUser().getId(),2.0));
//                map.put("rating1p",this.service.percentServiceMan(this.getCurShop().getId(),shopEmployee.getUser().getId(),1.0));
                serviceManList.add(map);


            }

            model.addAttribute("serviceManList",serviceManList);
        }
        //=======================




        model.addAttribute("nav_code","count");
        return this.viewPath("/count");
    }

}