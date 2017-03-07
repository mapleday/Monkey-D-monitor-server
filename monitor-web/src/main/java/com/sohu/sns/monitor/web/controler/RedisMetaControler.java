package com.sohu.sns.monitor.web.controler;

import com.sohu.sns.monitor.common.module.RedisMetaMemory;
import com.sohu.sns.monitor.common.services.RedisMetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
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

    @RequestMapping("/redisMeta")
    public String index(Model model) {
        return "redisMeta";
    }

//    @RequestMapping("/updateRedisMetaMemory")
//    @ResponseBody
//    public void updateRedisMetaMemory(RedisMetaMemory redisMetaMemory) {
//        redisMetaService.updateRedisMetaMemory(redisMetaMemory);
//    }

//    @RequestMapping("/addRedisMetaMemory")
//    @ResponseBody
//    public void addRedisMetaMemory(RedisMetaMemory redisMetaMemory) {
//        redisMetaService.addRedisMetaMemory(redisMetaMemory);
//    }

//    @RequestMapping("/deleteRedisMetaMemory")
//    @ResponseBody
//    public void deleteRedisMetaMemory(RedisMetaMemory redisMetaMemory) {
//        redisMetaService.deleteRedisMetaMemory(redisMetaMemory);
//    }

    @RequestMapping(value = "/getRedisMeta")
    @ResponseBody
    public Map  getRedisMeta(){
        HashMap<String,Object> map=new HashMap<String,Object>();
        map.put("data",redisMetaService.getRedisMeta());
        return map;
    }

    @ResponseBody
    @RequestMapping(value = "/updateRedisMeta",method = RequestMethod.POST)
    public Map updateRedisMeta(RedisMetaMemory redisMetaMemory){
        System.out.print(redisMetaMemory.getUpdateTime()+"YY");
        redisMetaService.updateRedisMeta(redisMetaMemory);
        Map<String,Object> map=new HashMap<String,Object>();
        List<RedisMetaMemory> list=new ArrayList<RedisMetaMemory>();
        list.add(redisMetaMemory);
        map.put("data",list);
        return map;
    }

    @ResponseBody
    @RequestMapping(value = "/deleteRedisMeta",method = RequestMethod.POST)
    public RedisMetaMemory  deleteRedisMeta(RedisMetaMemory redisMetaMemory){
        redisMetaService.deleteRedisMeta(redisMetaMemory);
        return new RedisMetaMemory();
    }

    @ResponseBody
    @RequestMapping(value = "/createRedisMeta",method = RequestMethod.POST)
    public  Map createRedisMeta( RedisMetaMemory redisMetaMemory){
        redisMetaService.createRedisMeta(redisMetaMemory);
        Map<String,Object> map=new HashMap<String,Object>();
        List<RedisMetaMemory> list=new ArrayList<RedisMetaMemory>();
        list.add(redisMetaMemory);
        map.put("data",list);
        return map;
    }
}
