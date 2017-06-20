package io.iotp.web.controller.iot;

import io.springbootstrap.core.api.ResponseData;
import io.springbootstrap.core.controller.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Iot test controller
 *
 * @author CD826
 * @since 1.0.0
 */
@Controller
@RequestMapping("/iot")
public class IotController extends AbstractController {
    /**
     * demo页面
     */
    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public String checkSignature(){
        String signature = this.getParameter("signature", "");
        String timestamp = this.getParameter("timestamp", "");
        String nonce = this.getParameter("nonce", "");
        String echostr = this.getParameter("echostr", "");

        System.out.println("signature = " + signature +", timestamp = " + timestamp + ", nonce = " + nonce + ", echostr = " + echostr);
        return echostr;
    }

    /**
     * demo页面
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseBody
    public ResponseData wechatNotify(HttpServletRequest request){
        System.out.println("wechat notify message.......................");
        int BUFFER_SIZE = 4096;
        try {
            request.setCharacterEncoding("UTF-8");
            try {
                InputStream inputStream = request.getInputStream();
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                byte[] data = new byte[BUFFER_SIZE];
                int count = -1;
                while((count = inputStream.read(data,0,BUFFER_SIZE)) != -1)
                    outStream.write(data, 0, count);

                data = null;
                System.out.println("wechat json = " + new String(outStream.toByteArray(),"ISO-8859-1"));
                System.out.println("wechat json = " + new String(outStream.toByteArray(),"UTF-8"));

                inputStream.close();
                inputStream = null;
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        ResponseData responseData = new ResponseData();
        responseData.put("error_code", 0);
        responseData.put("error_msg", "ok");
        return responseData;
    }


}
