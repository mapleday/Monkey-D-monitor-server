package com.sohu.sns.monitor.web.controler;

import com.sohu.sns.monitor.common.module.HttpResource;
import com.sohu.sns.monitor.common.services.HttpResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.ArrayList;
import java.util.List;

/**
 * author:jy
 * time:16-11-22上午11:44
 *
 * update by yw on 2017/2/9
 */
@Controller
public class MainController {
    @RequestMapping("/index")
    public String index(Model model) {
        return "index";
    }

//    @RequestMapping("/snsWatchUtil")
//    public  ModelAndView snsWatchUtil(){
//        return  new ModelAndView(new RedirectView("http://192.168.46.73:8080/"));
//
//    }

    @RequestMapping("/snsWatchUtil")
    public  String snsWatchUtil(){
        return "duty";
    }

    @RequestMapping("/login")
    public String login(){
        return "login";
    }


}
