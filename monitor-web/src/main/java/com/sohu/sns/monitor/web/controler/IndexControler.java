package com.sohu.sns.monitor.web.controler;

import com.sohu.sns.monitor.common.module.HttpResource;
import com.sohu.sns.monitor.common.services.HttpResourceService;

import com.sohucs.org.apache.commons.collections.map.HashedMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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
        //List<HttpResource> httpResourcelist = httpResourceService.getResources();
        //model.addAttribute("httpResourcelist",httpResourcelist);
        return "index";
    }


    @RequestMapping(value = "/updatehttpResource")
    @ResponseBody
    public int updatehttpResource(HttpResource httpResource) {
        return httpResourceService.updatehttpResource(httpResource);
    }

    /**
     * 增加资源
     * @param httpResource
     * @return
     */
    @RequestMapping(value = "/addhttpResource")
    @ResponseBody
    public int addhttpResource(HttpResource httpResource) {
        return httpResourceService.addhttpResource(httpResource);
    }

    /**
     * 删除资源
     * @param httpResource
     * @return
     */
    @RequestMapping(value = "/deletehttpResource")
    @ResponseBody
    public String deletehttpResource(HttpResource httpResource) {
        if(httpResourceService.deletehttpResource(httpResource)==1){
            return "remove";
        }
        return null;
    }



    /**
     * ajax异步请求， 返回list
     */
    @RequestMapping(value = "/getHttpResourcelist",produces="application/json")
    @ResponseBody
    public Object getHttpResourcelist() {
        Map<String,Object> result = new HashedMap();
        List<HttpResource> httpResourcelist = httpResourceService.getResources();
        result.put("data",httpResourcelist);
        result.put("options", new ArrayList());
        result.put("files",new ArrayList());
        return result;
    }
}
