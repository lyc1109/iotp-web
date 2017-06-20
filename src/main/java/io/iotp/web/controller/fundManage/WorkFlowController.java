package io.iotp.web.controller.fundManage;

import io.springbootstrap.core.controller.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/workflow")
public class WorkFlowController extends AbstractController {
    @RequestMapping(method = RequestMethod.GET)
    public String workflow(){
        return this.viewPath("/workflow");
    }

    @RequestMapping(value = "/expenseForm",method = RequestMethod.GET)
    public String expenseForm(){
        return this.viewPath("/expenseForm");
    }
}
