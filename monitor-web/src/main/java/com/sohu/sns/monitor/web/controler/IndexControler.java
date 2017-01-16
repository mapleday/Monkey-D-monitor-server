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
 */
@Controller
public class IndexControler {
    @Autowired
    HttpResourceService httpResourceService;

    @RequestMapping("/index")
    public String index(Model model) {
        List<HttpResource> httpResourcelist = httpResourceService.getResources();
        List<String> httpResourcelistName=new ArrayList<String>();
        for (HttpResource hr:httpResourcelist){
            System.out.println("MM---"+hr);


        }
        httpResourcelistName.add("id");
        httpResourcelistName.add("resourceName");
        httpResourcelistName.add("resourceAddress");
        httpResourcelistName.add("monitorTimeOut");
        httpResourcelistName.add("monitorInterval");
        httpResourcelistName.add("monitorTimes");
        model.addAttribute("httpResourcelist",httpResourcelist);
        model.addAttribute("httpResourcelistName",httpResourcelistName);

        return "index";
    }

//    @RequestMapping("/index3")
//    public String index3(){
//        return "index3";
//    }



}
