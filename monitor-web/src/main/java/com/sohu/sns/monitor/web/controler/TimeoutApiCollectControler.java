package com.sohu.sns.monitor.web.controler;

import com.sohu.sns.monitor.common.module.TimeoutApiCollect;
import com.sohu.sns.monitor.common.services.TimeoutApiCollectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/1/11.
 */

@Controller
public class TimeoutApiCollectControler {

    @Autowired
    TimeoutApiCollectService timeoutApiCollectService;

    @RequestMapping("/RedisMetaMemory")
    public String index(Model model) {
        return "redismetamemory";
    }



//    @RequestMapping(value = "/getTimeoutApiCollectList")
//    @ResponseBody
//    public Object getTimeoutApiCollectList(TimeoutApiCollect timeoutApiCollect) {
//        Map<String,Object> redisMetaMemoryListMap = new HashMap<String,Object>();
//        List<TimeoutApiCollect> redisMetaMemoryList =timeoutApiCollectService.
//        redisMetaMemoryListMap.put("data",redisMetaMemoryList);
//        return redisMetaMemoryListMap;
//    }

}
