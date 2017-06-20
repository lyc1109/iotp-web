package io.iotp.web.controller.help;

import io.iotp.help.dto.HelpCategoryDto;
import io.iotp.help.service.HelpCategoryService;
import io.iotp.help.service.HelpService;
import io.iotp.module.help.entity.Help;
import io.iotp.module.help.entity.HelpCategory;
import io.iotp.web.controller.IotpCrudController;
import io.springbootstrap.core.annotation.UserLogConfig;
import io.springbootstrap.core.annotation.UserLogable;
import io.springbootstrap.core.api.ResponseData;
import io.springbootstrap.core.exception.ErrorCode;
import io.springbootstrap.core.util.IdConfuseUtils;
import io.springbootstrap.core.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 帮助分类controller
 * @author wuhaohang
 * @create 2017-03-23 10:49
 */
@Controller
@RequestMapping("/help/category")
@UserLogConfig(moduleName = "帮助分类管理")
public class HelpCategoryController extends IotpCrudController<HelpCategory,HelpCategoryDto,HelpCategoryService>{
    @Autowired
    HelpService helpService;
    @Override
    public Page<HelpCategoryDto> getPageData(Pageable pageable, String searchPhrase) {
        Page<HelpCategory> helpCategories = this.service.getPage(pageable,searchPhrase);
        return this.mapToDtoPage(pageable,helpCategories);
    }

    @Override
    protected void onOpenCreateForm(Model model, HelpCategoryDto dto) {
        super.onOpenCreateForm(model, dto);
        String parentId = this.getParameter("parentId","");
        if(!StringUtils.isNullString(parentId)){
            HelpCategory helpCategory = this.service.load(IdConfuseUtils.decodeId(parentId));
            if(helpCategory != null){
                HelpCategoryDto helpCategoryDto = new HelpCategoryDto();
                helpCategoryDto.setId(helpCategory.getId()+"");
                helpCategoryDto.setName(helpCategory.getName());
                helpCategoryDto.setDes(helpCategory.getDes());
                dto.setParent(helpCategoryDto);
                dto.setParentFullName(helpCategory.getFullName());
            }

        }
    }

    /**
     *  检查分类是否已存在
     *
     * @param parentId
     * @param name
     * @return
     */
    @RequestMapping(value = "/isExist",method = RequestMethod.GET,headers="X-Requested-With")
    @ResponseBody
    @UserLogable(methodName = "更新校验")
    public ResponseData isExist(@RequestParam(defaultValue = "") String parentId,@RequestParam String name){
        ResponseData responseData = new ResponseData();
        HelpCategory helpCategory = this.service.findOneByName(name);
        boolean isExist = false;
        if(helpCategory != null){
            isExist = true;
        }
        responseData.put("isExist",isExist);
        return responseData;
    }

    /**
     * 删除前检查该分类下是否存在帮助文档或子分类
     * @param id
     * @return
     */
    @RequestMapping(value = "/isExistChild",method = RequestMethod.GET,headers = "X-Requested-With")
    @ResponseBody
    @UserLogable(methodName = "删除校验")
    public ResponseData isExistChild(@RequestParam String id){
        ResponseData responseData = new ResponseData();
        boolean isExistChild = false;
        String childType = "";
        if(StringUtils.hasText(id)){
            /*//子分类校验
            String hql = "select count(*) from HelpCategory h where h.parent = ? and h.status in (0)";
            HelpCategory helpCategory = new HelpCategory();
            helpCategory.setId(IdConfuseUtils.decodeId(parentId));
            Object[] values = new Object[]{helpCategory};
            Long size = this.service.getChildNums(hql,values);
            isExistChild = size > 0 ? true : false;
            childType = size > 0 ? "category":"";*/
            //帮助文档校验
            if(isExistChild == false){
                List<Help> helps = helpService.findByCategoryId(IdConfuseUtils.decodeId(id),null,null);
                if(helps.size()>0){
                    isExistChild = true;
                    childType = "help";
                }
            }
        }
        responseData.put("isExistChild",isExistChild);
        responseData.put("childType",childType);
        return responseData;
    }
    @RequestMapping(value = "/{id}/remove",method = RequestMethod.DELETE,headers="X-Requested-With")
    @ResponseBody
    @UserLogable(methodName = "删除")
    public ResponseData delete(@PathVariable String id){
        ResponseData responseData = new ResponseData();
        try {
            HelpCategory helpCategory = this.service.load(IdConfuseUtils.decodeId(id));
            if(helpCategory == null){
                responseData.setReturnCode(9);
                responseData.setReturnMsg("不存在!");
                return responseData;
            }
            helpCategory.setStatus(com.xiaochenghudong.core.util.Constants.STATUS_DELETED);
            this.service.save(helpCategory);
        }catch (Exception e){
            responseData = new ResponseData(ErrorCode.BAD_REQUEST);
        }
        return responseData;
    }

}
