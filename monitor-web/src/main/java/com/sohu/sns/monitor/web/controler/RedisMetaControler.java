package com.sohu.sns.monitor.web.controler;

import com.sohu.sns.monitor.common.module.RedisMetaMemory;
import com.sohu.sns.monitor.common.services.RedisMetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/1/11.
 */

@Controller
public class RedisMetaControler {

    @Autowired
    RedisMetaService redisMetaService;

    @RequestMapping("/redisMetaMemory")
    public String index(Model model) {
        return "redisMetaMemory";
    }

    @RequestMapping("/updateRedisMetaMemory")
    @ResponseBody
    public void updateRedisMetaMemory(RedisMetaMemory redisMetaMemory) {
        redisMetaService.updateRedisMetaMemory(redisMetaMemory);
    }

    @RequestMapping("/addRedisMetaMemory")
    @ResponseBody
    public void addRedisMetaMemory(RedisMetaMemory redisMetaMemory) {
        redisMetaService.addRedisMetaMemory(redisMetaMemory);
    }

    @RequestMapping("/deleteRedisMetaMemory")
    @ResponseBody
    public void deleteRedisMetaMemory(RedisMetaMemory redisMetaMemory) {
        redisMetaService.deleteRedisMetaMemory(redisMetaMemory);
    }


    @RequestMapping(value = "/getRedisMetaMemoryList")
    @ResponseBody
    public Object getRedisMetaMemoryList(RedisMetaMemory redisMetaMemory) {
        Map<String,Object> redisMetaMemoryListMap = new HashMap<String,Object>();
        List<RedisMetaMemory> redisMetaMemoryList =redisMetaService.getRedisMetaMemory();
        redisMetaMemoryListMap.put("data",redisMetaMemoryList);
        return redisMetaMemoryListMap;
    }
}
