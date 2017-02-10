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

    public void updateMqttServerAddress(MqttServerAddress msa){
        mqttServerAddressDao.updateMqtt(msa);
    }

    public void deleteMqttServerAddress(MqttServerAddress msa){
        mqttServerAddressDao.deleteMqtt(msa);
    }

    public void createMqttServerAddress(MqttServerAddress msa){
        mqttServerAddressDao.createMqtt(msa);
    }
}
