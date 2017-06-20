package io.iotp.web.controller.fundManage;

import io.springbootstrap.core.controller.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * fundManageController
 *
 * @author lyc
 * @since 1.0.0
 */
@Controller
@RequestMapping("")
public class FundManageController extends AbstractController{

    @RequestMapping(value = "/shop/expenseProcessForm", method = RequestMethod.GET)
    public String fundManage(){
        return this.viewPath("/fundManage/fundManage");
    }

    @RequestMapping(value = "/shop/expenseProcessForm/addExpend", method = RequestMethod.GET)
    public String addExpend(){
        return this.viewPath("/fundManage/addExpend");
    }

//    @RequestMapping(value = "/approval", method = RequestMethod.GET)
//    public String expendApproval(){
//        return this.viewPath("/expendApproval");
//    }

    @RequestMapping(value = "/shop/expenseProcessForm/{entityId}", method = RequestMethod.GET)
    public String viewApproval(Model model, @PathVariable String entityId){
        model.addAttribute("entityId", entityId);
        return this.viewPath("/fundManage/expendApproval");
    }
}
