package com.sohu.sns.monitor.web.controler;

import com.sohu.sns.monitor.common.module.HttpResource;
import com.sohu.sns.monitor.common.services.HttpResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yw on 2017/1/13.
 */
@Controller
public class JsonControler {
    @Autowired
    HttpResourceService httpResourceService;
    @ResponseBody
    @RequestMapping(value="/getHttpResource")
    public Map getHttpResource(){
        HashMap<String,Object> map=new HashMap<String,Object>();
        map.put("data",httpResourceService.getResources());
        map.put("options","");
        map.put("files","");
        return map;
    }

    @RequestMapping(value = "/updateResource",method = RequestMethod.POST)
    public void updateResource(HttpResource[] hr){
        httpResourceService.updateResource(hr[0]);
//        , @RequestParam(value = "id") int id
    }

}
