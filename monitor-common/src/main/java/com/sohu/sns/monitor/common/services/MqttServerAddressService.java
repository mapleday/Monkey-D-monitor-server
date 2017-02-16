package com.sohu.sns.monitor.common.services;

import com.sohu.sns.monitor.common.dao.mqttServerAddress.MqttServerAddressDao;
import com.sohu.sns.monitor.common.module.MqttServerAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by yw on 2017/2/8.
 */
@Component
public class MqttServerAddressService {
    @Autowired
    MqttServerAddressDao mqttServerAddressDao;

    public List<MqttServerAddress> getMqttServerAddress(){
        return mqttServerAddressDao.getMqtt();
    }

    public void updateMqttServerAddress(MqttServerAddress mqttServerAddress){
        mqttServerAddressDao.updateMqtt(mqttServerAddress);
    }

    public void deleteMqttServerAddress(MqttServerAddress mqttServerAddress){
        mqttServerAddressDao.deleteMqtt(mqttServerAddress);
    }

    public void createMqttServerAddress(MqttServerAddress mqttServerAddress){
        mqttServerAddressDao.createMqtt(mqttServerAddress);
    }
}
