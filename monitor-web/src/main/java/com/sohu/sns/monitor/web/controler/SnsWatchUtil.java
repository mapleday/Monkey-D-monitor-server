package com.sohu.sns.monitor.web.controler;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by Administrator on 2017/2/20.
 */
@Controller
public class SnsWatchUtil {
    @RequestMapping("/snsWatchUtil")
    public  String snsWatchUtil(){
        return "duty";
    }

    //    @RequestMapping("/snsWatchUtil")
//    public  ModelAndView snsWatchUtil(){
//        return  new ModelAndView(new RedirectView("http://192.168.46.73:8080/"));
//
//    }

}
