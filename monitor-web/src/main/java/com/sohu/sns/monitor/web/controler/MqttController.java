package com.sohu.sns.monitor.web.controler;

import com.sohu.sns.monitor.common.module.HttpResource;
import com.sohu.sns.monitor.common.module.MqttServerAddress;
import com.sohu.sns.monitor.common.services.MqttServerAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yw on 2017/2/9.
 */
@Controller
public class MqttController {
    @Autowired
    MqttServerAddressService mqttServerAddressService;

    @RequestMapping("/mqtt")
    public String mqttServerAddress(){
        return  "mqttServerAddress";
    }

    @ResponseBody
    @RequestMapping(value="/getMqtt")
    public Map getMqttServerAddress(){
        HashMap<String,Object> map=new HashMap<String,Object>();
        map.put("data",mqttServerAddressService.getMqttServerAddress());
        map.put("options","");
        map.put("files","");
        return map;
    }

    @ResponseBody
    @RequestMapping(value = "/updateMqtt",method = RequestMethod.POST)
    public Map updateMqtt(MqttServerAddress msa){
        mqttServerAddressService.updateMqttServerAddress(msa);
        Map<String,Object> map=new HashMap<String,Object>();
        ArrayList<MqttServerAddress> list=new ArrayList<MqttServerAddress>();
        list.add(msa);
        map.put("data",list);
        return map;
    }

    @ResponseBody
    @RequestMapping(value = "/deleteMqtt",method = RequestMethod.POST)
    public MqttServerAddress  deleteMqtt(MqttServerAddress msa){
        mqttServerAddressService.deleteMqttServerAddress(msa);
        return new MqttServerAddress();
    }

    @ResponseBody
    @RequestMapping(value = "/createMqtt",method = RequestMethod.POST)
    public  Map createMqtt( MqttServerAddress msa){
        mqttServerAddressService.createMqttServerAddress(msa);
        Map<String,Object> map=new HashMap<String,Object>();
        ArrayList<MqttServerAddress> list=new ArrayList<MqttServerAddress>();
        list.add(msa);
        map.put("data",list);
        return map;
    }
}
