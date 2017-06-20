/*
 * Copyright (c) 2016 - 广东小哈科技股份有限公司 
 * All rights reserved.
 *
 * Created on 2017-06-12
 */
package io.iotp.web.controller.help;

import io.iotp.core.security.IotpSessionContext;
import io.iotp.help.dto.HelpCategoryDto;
import io.iotp.help.dto.HelpDto;
import io.iotp.help.mapper.HelpCategoryEntityDtoMapper;
import io.iotp.help.mapper.HelpEntityDtoMapper;
import io.iotp.help.service.HelpCategoryService;
import io.iotp.help.service.HelpService;
import io.iotp.module.help.entity.Help;
import io.iotp.module.help.entity.HelpCategory;
import io.springbootstrap.core.annotation.UserLogConfig;
import io.springbootstrap.core.annotation.UserLogable;
import io.springbootstrap.core.api.ResponseData;
import io.springbootstrap.core.controller.AbstractController;
import io.springbootstrap.core.util.IdConfuseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 帮助中心Controller
 *
 * @author wuhaohang
 * @since 2.0.0
 */
@Controller
@RequestMapping("/helpCenter")
@UserLogConfig(moduleName = "帮助中心")
public class HelpCenterController extends AbstractController{
    @Autowired
    private HelpService service;
    @Autowired
    private HelpCategoryService helpCategoryService;
    @Autowired
    private HelpEntityDtoMapper helpEntityDtoMapper;
    @Autowired
    private HelpCategoryEntityDtoMapper helpCategoryEntityDtoMapper;

    /**
     * 帮助中心首页
     * @param model
     * @param pageCode
     * @return
     */
    @RequestMapping(value = "",method = RequestMethod.GET)
    @UserLogable(methodName = "帮助中心首页")
    public String list(Model model, @RequestParam(value = "pageCode",defaultValue = "help") String pageCode,@RequestParam(value = "role",defaultValue = "CS") String role) {
        List<HelpCategoryDto> helpCategoryDtos = helpCategoryEntityDtoMapper.mapToDtos(this.service.findAllCategories(),HelpCategoryDto.class);
        Map<String,List<HelpDto>> helps = new HashMap<>();
        List<HelpCategoryDto> entity = new ArrayList<>();  //保存没有帮助文档的分类
        for(HelpCategoryDto dto : helpCategoryDtos){
            String[] roles = new String []{role.toUpperCase()};
            List<HelpDto> HelpDtos = this.mapToDtos(this.service.findByCategoryId(IdConfuseUtils.decodeId(dto.getId()),roles,pageCode));
            if(HelpDtos == null || HelpDtos.size() == 0){
                entity.add(dto);
            }
            helps.put(dto.getId(),HelpDtos);
        }
        helpCategoryDtos.removeAll(entity);

        //add
        model.addAttribute("entity",helpCategoryDtos);
        model.addAttribute("helps",helps);
        model.addAttribute("pageCode",pageCode);
        model.addAttribute("role",role);
        return this.viewPath("/index");
    }

    /**
     * 进入帮助详情页面
     * @param model
     * @param id  帮助分类id
     * @param pageCode
     * @return
     */
    @RequestMapping(value = "/{categoryId}/{id}")
    @UserLogable(methodName = "文档详情")
    public String view(Model model, @PathVariable String categoryId, @PathVariable String id, @RequestParam(value = "pageCode",defaultValue = "help") String pageCode,@RequestParam(value = "role",defaultValue = "CS") String role){
        //当前分类信息
        HelpCategory helpCategory = helpCategoryService.load(IdConfuseUtils.decodeId(categoryId));
        HelpCategoryDto helpCategoryDto = helpCategoryEntityDtoMapper.mapToDto(helpCategory,HelpCategoryDto.class);
        model.addAttribute("helpCategory",helpCategoryDto);

        //当前分类下的帮助
        String[] roles = new String []{role.toUpperCase()};
        List<Help> helps = this.service.findByCategoryId(IdConfuseUtils.decodeId(categoryId),roles,pageCode);
        if((Help.TYPE_HELP).equals(pageCode)){
            model.addAttribute("helps",this.mapToDtos(helps));
        }
        if((Help.TYPE_FAQ).equals(pageCode)){
            model.addAttribute("faqs",this.mapToDtos(helps));
        }

        //查询的help
        Help help = this.service.load(IdConfuseUtils.decodeId(id));
        HelpDto helpDto = this.mapToDto(help);
        model.addAttribute("help",helpDto);

        //查询的help的相关帮助
        List<Help> helpList = new ArrayList<>();
        List<Help> faqList = new ArrayList<>();
        for(Help h : help.getHelpList()){
            if((Help.TYPE_FAQ).equals(h.getType())){
                faqList.add(h);
            }else if((Help.TYPE_HELP).equals(h.getType())){
                helpList.add(h);
            }
        }
        model.addAttribute("helpList",this.mapToDtos(helpList));
        model.addAttribute("faqList",this.mapToDtos(faqList));

        //传参
        model.addAttribute("pageCode",pageCode);
        model.addAttribute("role",role);
        return this.viewPath("/details");
    }

    /**
     * 帮助中心搜索关键字
     * @param model
     * @param search
     * @return
     */
    @RequestMapping(value = "/search/list",method = RequestMethod.GET)
    @UserLogable(methodName = "搜索关键字")
    public String getHelpsBySearch(Model model, @RequestParam(name = "keyword") String search,@RequestParam(value = "pageCode",defaultValue = "") String pageCode,@RequestParam(defaultValue = "CS") String role){
        List<Help> helps = this.service.findAllByKeyWord(search,role,pageCode);

        //查询的help的相关帮助
        List<Help> helpList = new ArrayList<>();
        List<Help> faqList = new ArrayList<>();
        for(Help h : helps){
            if((Help.TYPE_FAQ).equals(h.getType())){
                faqList.add(h);
            }else if((Help.TYPE_HELP).equals(h.getType())){
                helpList.add(h);
            }
        }
        model.addAttribute("helpList",this.mapToDtos(helpList));
        model.addAttribute("faqList",this.mapToDtos(faqList));
        model.addAttribute("keyword",search);
        model.addAttribute("pageCode",pageCode);
        model.addAttribute("role",role);
        model.addAttribute("countHelp",this.service.count(search,role,Help.TYPE_HELP));
        model.addAttribute("countFaq",this.service.count(search,role,Help.TYPE_FAQ));
        return this.viewPath("/search");
    }


    /**
     * 根据编码获取文档
     *
     * @param code 编码
     * @return
     */
    @RequestMapping(value = "/code",method = RequestMethod.GET,headers = "X-Requested-With")
    @ResponseBody
    @UserLogable(methodName = "根据编码获取文档")
    public ResponseData getHelpByCode(@RequestParam(name = "code") String code){
        ResponseData responseData = new ResponseData();
        Help help = this.service.findByCode(code);
        if(help == null){
            responseData.setReturnCode(ResponseData.ERROR_RETURN_CODE);
            responseData.setReturnMsg("不存在!");
            return responseData;
        }
        List<Help> helpList = new ArrayList<>();
        List<Help> faqList = new ArrayList<>();
        List<Help> list = help.getHelpList();
        for(Help h : list){
            if("faq".equalsIgnoreCase(h.getType())){
                faqList.add(h);
            }else if("help".equalsIgnoreCase(h.getType())){
                helpList.add(h);
            }
        }
        responseData.put("help",this.mapToDto(help));
        responseData.put("helpList",this.mapToDtos(helpList));
        responseData.put("faqList",this.mapToDtos(faqList));
        responseData.put("shopType", IotpSessionContext.getShop().getShopType());
        return responseData;
    }


    /**
     * 版面帮助-产品动态-获取最新日志
     *
     * @param num  查询条数
     * @return
     */
    @RequestMapping(value = "/pageHelp/UpdateLog",method = RequestMethod.GET,headers = "X-Requested-With")
    @ResponseBody
    @UserLogable(methodName = "版面帮助-产品动态-获取最新日志")
    public ResponseData getUpdateLog(@RequestParam(defaultValue = "-1") long num,@RequestParam(defaultValue = "list") String type) throws IOException {
        ResponseData responseData = new ResponseData();
        String[] patterns = null;
        if("list".equals(type)){
            patterns = new String[]{"(^\\*)([^\\*]+)"};
        }
        List<String> list = readTxtFileIntoStringArrList(HelpController.class.getResource("/CHANGELOG.md").getPath(),patterns,num);
        List<String> rows = new ArrayList<>();
        for(String str:list){
            rows.add(str.substring(1));
        }
        responseData.put("rows",rows);
        return responseData;
    }

    /**
     * 更新日志对话框
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/pageHelp/UpdateLog/list",method = RequestMethod.GET,headers="X-Requested-With")
    @UserLogable(methodName = "版面帮助-产品动态-更新日志")
    public String toUpdateLogDlg(Model model){
        List<String> list = readTxtFileIntoStringArrList(HelpController.class.getResource("/CHANGELOG.md").getPath(),null,-1);
        StringBuffer content = new StringBuffer();
        for(String str:list){
            content.append(str).append("\r\n");
        }
        model.addAttribute("content",content.toString().trim());
        return this.viewPath("/updateLogDlg");
    }





    private List<HelpDto> mapToDtos(List<Help> helpList){
        return this.helpEntityDtoMapper.mapToDtos(helpList,HelpDto.class);
    }
    private HelpDto mapToDto(Help help){
        return this.helpEntityDtoMapper.mapToDto(help,HelpDto.class);
    }




    // ================ tools =======================
    /**
     * 检验字符串是否匹配指定pattern
     * @param patterns
     * @param content
     * @return
     */
    public  boolean isMatch(String[] patterns,String content) {
        boolean bn = false;
        for(String pattern:patterns){
            if(bn == false){
                bn = Pattern.matches(pattern,content);
            }
        }
        return bn;
    }

    /**
     * 功能：Java读取txt文件的内容 步骤：1：先获得文件句柄 2：获得文件句柄当做是输入一个字节码流，需要对这个输入流进行读取
     * 3：读取到输入流后，需要读取生成字节流 4：一行一行的输出。readline()。 备注：需要考虑的是异常情况
     *
     * @param filePath
     *            文件路径[到达文件:如： D:\aa.txt]
     * @return 将这个文件按照每一行切割成数组存放到list中。
     */
    public  List<String> readTxtFileIntoStringArrList(String filePath,String[] patterns,long num)
    {
        List<String> list = new ArrayList<String>();
        try
        {
            String encoding = "UTF-8";
            File file = new File(filePath);
            if (file.isFile() && file.exists())
            { // 判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file), encoding);// 考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                long i = 0;
                while ((lineTxt = bufferedReader.readLine()) != null)
                {
                    if(num>0 && i == num){
                        break;
                    }
                    if(patterns != null && patterns.length>0){
                        if(isMatch(patterns,lineTxt)){
                            list.add(lineTxt);
                            i++;
                        }
                    }else{
                        list.add(lineTxt);
                        i++;
                    }

                }
                bufferedReader.close();
                read.close();
            }
            else
            {
                System.out.println("找不到指定的文件");
            }
        }
        catch (Exception e)
        {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
        return list;
    }
}