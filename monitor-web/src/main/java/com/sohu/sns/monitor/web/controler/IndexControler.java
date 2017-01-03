package com.sohu.sns.monitor.web.controler;

import com.sohu.sns.monitor.common.module.HttpResource;
import com.sohu.sns.monitor.common.services.HttpResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

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
        model.addAttribute("httpResourcelist",httpResourcelist);
        return "index";
    }
}
