package com.sohu.sns.monitor.web.controler;

import com.sohu.sns.monitor.common.module.HttpResource;
import com.sohu.sns.monitor.common.services.HttpResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * author:jy
 * time:16-11-22上午11:44
 *
 * update by yw on 2017/2/9
 */
@Controller
public class IndexControler {
    @RequestMapping("/index")
    public String index(Model model) {
        return "index";
    }

    @RequestMapping("/HttpResource")
    public String HttpResource(){
        return "HttpResource";
    }

    @RequestMapping("/mqtt")
    public String mqttServerAddress(){
        return  "mqttServerAddress";
    }

    @RequestMapping("/notifyPerson")
    public String notifyPerson(){
        return "notifyPerson";
    }


}
