package com.sohu.sns.monitor.web.controler;

import net.rossillo.spring.web.mvc.CacheControl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * author:jy
 * time:16-11-22上午11:44
 * <p>
 * update by yw on 2017/2/9
 */
@Controller
public class IndexController {
    @RequestMapping(value = "/index", method = RequestMethod.GET)
    @CacheControl()
    public String index(HttpServletRequest request, HttpServletResponse response) {
        return "index";
    }


    @RequestMapping(value = "/")
    public String index(){
        return "index";
    }
}
