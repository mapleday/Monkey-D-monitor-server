package com.sohu.sns.monitor.common.services;

import com.sohu.sns.monitor.common.dao.mqttMonitor.MqttMonitorDao;
import com.sohu.sns.monitor.common.module.MqttServerAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by Administrator on 2017/1/9.
 */

@Component
public class MqttMonitorService {
    @Autowired
    MqttMonitorDao mqttMonitorDao;

    public List<MqttServerAddress> getServers(){
        return mqttMonitorDao.getServers();
    }



}
