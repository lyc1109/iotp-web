/*
 * Copyright (c) 2016 - 广东小哈科技股份有限公司 
 * All rights reserved.
 *
 * Created on 2017-03-22
 */
package io.iotp.web.controller.help;

import io.iotp.help.dto.HelpCategoryDto;
import io.iotp.help.dto.HelpDto;
import io.iotp.help.mapper.HelpCategoryEntityDtoMapper;
import io.iotp.help.service.HelpCategoryService;
import io.iotp.help.service.HelpService;
import io.iotp.module.help.entity.Help;
import io.iotp.module.help.entity.HelpCategory;
import io.iotp.module.help.repository.HelpCategoryRepository;
import io.iotp.product.service.ProductFaqService;
import io.iotp.web.controller.IotpCrudController;
import io.springbootstrap.core.annotation.UserLogConfig;
import io.springbootstrap.core.util.IdConfuseUtils;
import io.springbootstrap.core.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 帮助管理Controller
 *
 * @author huchiwei
 * @author wuhaohang
 * @since 2.0.0
 */
@Controller
@RequestMapping("/help/content")
@UserLogConfig(moduleName = "帮助管理")
public class HelpController extends IotpCrudController<Help, HelpDto, HelpService> {

    @Autowired
    HelpCategoryRepository helpCategoryRepository;
    @Autowired
    HelpCategoryService helpCategoryService;
    @Autowired
    ProductFaqService productFaqService;



    @Override
    public Page<HelpDto> getPageData(Pageable pageable, String searchPhrase) {
        String helpCategory = this.getParameter("helpCategory","");
        String helpType = this.getParameter("pageCode",Help.TYPE_HELP);
        long categoryId = StringUtils.hasText(helpCategory) ? IdConfuseUtils.decodeId(helpCategory) : 0;
        Page<Help> helps = this.service.getPage(pageable,searchPhrase,categoryId,helpType);
        return this.mapToDtoPage(pageable,helps);
    }

    @Override
    protected void onOpenForm(Model model, HelpDto dto) {
        super.onOpenForm(model, dto);
        this.forwardParameters("pageCode","helpCategory","helpType");
    }

    @Override
    protected void onList() {
        super.onList();
        List<HelpCategory> categories = helpCategoryRepository.findAll(new int[]{0});
        List<HelpCategoryDto> helpCategoryDtolist = new HelpCategoryEntityDtoMapper().mapToDtos(categories,HelpCategoryDto.class);
        this.setAttribute("categories",helpCategoryDtolist);
        this.getRequest().setAttribute("pageCode",this.getParameter("pageCode",Help.TYPE_HELP));
        this.forwardParameters("helpCategory","helpType");
    }

    /**
     * 打开创建页面所要附加的处理
     *
     * @param model model对象
     * @param dto   dto对象
     */
    @Override
    protected void onOpenCreateForm(Model model, HelpDto dto) {
        super.onOpenCreateForm(model, dto);
        List<HelpCategory> categories = helpCategoryRepository.findAll(new int[]{0});
        List<HelpCategoryDto> helpCategoryDtolist = new HelpCategoryEntityDtoMapper().mapToDtos(categories,HelpCategoryDto.class);
        model.addAttribute("categories",helpCategoryDtolist);

    }

    @Override
    protected void onOpenViewForm(Model model, HelpDto dto) {
        super.onOpenViewForm(model, dto);
        Help help = this.service.load(IdConfuseUtils.decodeId(dto.getId()));
        modelAddProperty(help.getHelpList(),model);
    }

    /**
     * 打开编辑页面所要附加的处理
     *
     * @param model model对象
     * @param dto   dto对象
     */
    @Override
    protected void onOpenEditForm(Model model, HelpDto dto) {
        super.onOpenEditForm(model, dto);
        List<HelpCategory> categories = helpCategoryRepository.findAll(new int[]{0});
        List<HelpCategoryDto> helpCategoryDtolist = new HelpCategoryEntityDtoMapper().mapToDtos(categories,HelpCategoryDto.class);
        Help help = this.service.load(IdConfuseUtils.decodeId(dto.getId()));
        modelAddProperty(help.getHelpList(),model);
        model.addAttribute("categories",helpCategoryDtolist);
    }

    /**
     * 保存前对需要保存实体所附加的额外处理动作
     *
     * @param entity 所要保存的实体对象
     */
    @Override
    protected void onSaveEntity(Help entity) {
        super.onSaveEntity(entity);
        Boolean isNew = entity.isNew();
        if(isNew){
            entity.setCreaterId(this.getCurUser().getId());
            entity.setLastModifyUserId(this.getCurUser().getId());
        }else{
            entity.setLastModifyUserId(this.getCurUser().getId());
            entity.setLastModifyTime(new Date());
        }

        String normalCode = "00000000";


        String helpType = this.getParameter("pageCode",Help.TYPE_HELP);

        if((Help.TYPE_PAGE).equals(helpType)){
            //================版面帮助======================
            String code = this.toCode(entity.getCode())!=null ? this.toCode(entity.getCode()) : normalCode;
            //更新版面帮助前重置编码
            if(!normalCode.equals(code)){
                Help help = this.service.findByCode(code);
                if(help!=null){
                    help.setCode(normalCode);
                    this.service.save(help);
                }
            }
            entity.setCode(code);
            entity.setHelpCategory(null);
            //=============================================
        }else{
            entity.setCode(normalCode);
        }
        //保存相关帮助/问题
        String[] helpIdList = this.getRequest().getParameterValues("helpId");
        List<Help> helpList = new ArrayList<>();
        //关联相关帮助和问题
        if(helpIdList != null && helpIdList.length > 0){
            for(String helpId : helpIdList){
                Help help = this.service.load(IdConfuseUtils.decodeId(helpId));
                if(help != null){
                    helpList.add(help);
                }
            }
        }else{
            helpList = null;
        }
        entity.setHelpList(helpList);

    }
    @Override
    public String save(Model model, @PathVariable String entityId, @ModelAttribute HelpDto dto, RedirectAttributes redirectAttributes) {
        super.save(model, entityId, dto, redirectAttributes);
        redirectAttributes.addFlashAttribute("pageCode",this.getParameter("pageCode",Help.TYPE_HELP));
        return "redirect:" + this.getBasePath();
    }


    /**
     * 获取help编码
     * @param flag
     * @return
     */
    public String toCode(String flag){
        if(flag == null){
            return null;
        }
        if(!flag.startsWith("help-page-")){
            flag = null;
        }
        return flag;
    }

    /**
     * model添加相关帮助/问题 数据
     * @param list
     * @param model
     */
    public void modelAddProperty(List<Help> list,Model model){
        if(list == null){
            return;
        }
        List<Help> helpList = new ArrayList<>();
        List<Help> faqList = new ArrayList<>();
        for(Help h : list){
            if("faq".equalsIgnoreCase(h.getType())){
                faqList.add(h);
            }else if("help".equalsIgnoreCase(h.getType())){
                helpList.add(h);
            }
        }
        model.addAttribute("helpList",this.mapToDtos(helpList));
        model.addAttribute("faqList",this.mapToDtos(faqList));
        if(helpList.size()==0 && faqList.size()==0){
            model.addAttribute("hasRelevant","false");
        }
    }

    //=============RequestMapping methods =================


    /**
     * 选择帮助
     *
     * @param model model
     * @return 对话框路径
     */
    @RequestMapping(value = "/selectHelps", method = RequestMethod.GET)
    public String selectProductDlg(Model model){
        return this.viewPath("/selectHelpDlg::helpselectGrid");
    }

}
