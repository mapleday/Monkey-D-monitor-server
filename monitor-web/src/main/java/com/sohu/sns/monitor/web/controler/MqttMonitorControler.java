package com.sohu.sns.monitor.web.controler;

import com.sohu.sns.monitor.common.module.MqttServerAddress;
import com.sohu.sns.monitor.common.services.MqttMonitorService;
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
public class MqttMonitorControler {
    @Autowired
    MqttMonitorService mqttMonitorService;

    @RequestMapping("/MqttServerAddress")
    public String index(Model model) {
        return "mqttserveraddress";
    }

    @RequestMapping(value = "/updateMqttServerAddress")
    @ResponseBody
    public int updateMqttServerAddress(MqttServerAddress mqttServerAddress) {
        return mqttMonitorService.updateMqttServerAddress(mqttServerAddress);
    }

    @RequestMapping(value = "/addMqttServerAddress")
    @ResponseBody
    public int addMqttServerAddress(MqttServerAddress mqttServerAddress) {
        return mqttMonitorService.addMqttServerAddress(mqttServerAddress);
    }

    @RequestMapping(value = "/deleteMqttServerAddress")
    @ResponseBody
    public int deleteMqttServerAddress(MqttServerAddress mqttServerAddress) {
        return mqttMonitorService.addMqttServerAddress(mqttServerAddress);
    }



    @RequestMapping(value = "/getMqttServerAddressList")
    @ResponseBody
    public Object getMqttServerAddressList(MqttServerAddress mqttServerAddress) {
        Map<String,Object> mqttServerAddressListMap = new HashMap<String,Object>();
        List<MqttServerAddress> mqttServerAddressList = mqttMonitorService.getServers();
        mqttServerAddressListMap.put("data",mqttServerAddress);
        return mqttServerAddressListMap;
    }

}
