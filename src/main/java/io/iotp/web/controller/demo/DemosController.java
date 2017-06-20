package io.iotp.web.controller.demo;

import com.xiaochenghudong.core.account.service.impl.UserRepositoryService;
import io.springbootstrap.core.api.ResponseData;
import io.springbootstrap.core.controller.AbstractController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * DemoController
 *
 * @author huchiwei
 * @since 1.0.0
 */
@Controller
@RequestMapping("/demo")
public class  DemosController extends AbstractController{

    @Autowired
    private UserRepositoryService userService;

    /**
     * demo页面
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    public String index(){
        return this.viewPath("/index");
    }

    /**
     * BootGrid获取分页数据
     * @return
     */
    @RequestMapping(value = "/users", method = RequestMethod.GET, headers="X-Requested-With")
    @ResponseBody
    public ResponseData page(Pageable pageable, @RequestParam(required = false) String searchPhrase){
        Page page = this.userService.getPage(pageable);
        ResponseData responseData = new ResponseData(0, "");
        responseData.put("current", (pageable.getPageNumber()+1));
        responseData.put("rowCount", pageable.getPageSize());
        responseData.put("total", page.getTotalElements());
        responseData.put("rows", page.getContent());
        return responseData;
    }

    @RequestMapping(value = "/default", method = RequestMethod.GET)
    public String defaultPage(){
        return this.viewPath("/default");
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String listPage(){
        return this.viewPath("/list");
    }

    @RequestMapping(value = "/list/help", method = RequestMethod.GET)
    public String listHelpPage(){
        return this.viewPath("/list-help");
    }

    @RequestMapping(value = "/form", method = RequestMethod.GET)
    public String formPage(){
        return this.viewPath("/form");
    }

    @RequestMapping(value = "/form/help", method = RequestMethod.GET)
    public String formHelpPage(){
        return this.viewPath("/form-help");
    }

    @RequestMapping(value = "/view", method = RequestMethod.GET)
    public String viewPage(){
        return this.viewPath("/view");
    }

    @RequestMapping(value = "/productLease", method = RequestMethod.GET)
    public String productLease(){ return this.viewPath("/vue/product-lease");}

    @RequestMapping(value = "/productLease/addProductLease", method = RequestMethod.GET)
    public String addProductLease(){
        return this.viewPath("/vue/addProductLease");
    }

    @RequestMapping(value = "/fundManage", method = RequestMethod.GET)
    public String fundManage(){
        return this.viewPath("/vue/fundManage");
    }

    @RequestMapping(value = "/fundManage/expendApproval", method = RequestMethod.GET)
    public String expendApproval(){
        return this.viewPath("/vue/expendApproval");
    }

    @RequestMapping(value = "/fundManage/addExpend", method = RequestMethod.GET)
    public String addExpend(){
        return this.viewPath("/vue/addExpend");
    }

    /**
     * 转到对话框例子页面
     * @return
     */
    @RequestMapping(value = "/dialog", method = RequestMethod.GET)
    public String smodal(){
        return this.viewPath("/dialog");
    }

    /**
     * 异步加载对话框内容
     * @return
     */
    @RequestMapping(value = "/dialog/content", method = RequestMethod.GET)
    public String smodalContent(){
        return this.viewPath("/dialog-content");
    }

    /**
     * 图片素材选择
     * @return
     */
    @RequestMapping(value = "/imagePicker", method = RequestMethod.GET)
    public String imagePicker(){
        return this.viewPath("/imagePicker");
    }

    /**
     * vue grid
     * @return
     */

    /**
     * vue form
     * @return
     */
    @RequestMapping(value = "/vue/form", method = RequestMethod.GET)
    public String vueForm(){
        return this.viewPath("/vue/form");
    }

    @RequestMapping(value = "/single", method = RequestMethod.GET)
    public String single(){
        return this.viewPath("/single");
    }
}
