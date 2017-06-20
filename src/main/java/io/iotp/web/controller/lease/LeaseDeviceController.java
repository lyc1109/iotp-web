package io.iotp.web.controller.lease;

import io.springbootstrap.core.controller.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * LeaseDeviceController
 *
 * @author lyc
 * @since 1.0.0
 */
@Controller
@RequestMapping("/lease")
public class LeaseDeviceController extends AbstractController{
    @RequestMapping(value = "/devices", method = RequestMethod.GET)
    public String leaseDevice(){
        return this.viewPath("/devices/device");
    }

//    @RequestMapping(value = "/devices/detail", method = RequestMethod.GET)
//    public String leaseDeviceDetail(){
//        return this.viewPath("/devices/deviceDetail");
//    }

    @RequestMapping(value = "/devices/{entityId}", method = RequestMethod.GET)
    public String leaseDeviceDetail(Model model, @PathVariable String entityId){
        model.addAttribute("entityId", entityId);
        return this.viewPath("/devices/deviceDetail");
    }

//    @RequestMapping(value = "/detail/rechargeorders", method = RequestMethod.GET)
//    public String leaseDeviceRechargeorders(){
//        return this.viewPath("/rechargeorders");
//    }
    @RequestMapping(value = "/rechargeOrder/{entityId}", method = RequestMethod.GET)
    public String leaseDeviceRechargeorders(Model model, @PathVariable String entityId){
        model.addAttribute("entityId", entityId);
        return this.viewPath("/devices/rechargeorders");
    }
}
