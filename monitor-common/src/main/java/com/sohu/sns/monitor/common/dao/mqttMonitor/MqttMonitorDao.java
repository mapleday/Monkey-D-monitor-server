package com.sohu.sns.monitor.common.dao.mqttMonitor;

import com.sohu.sns.monitor.common.module.MqttServerAddress;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * author:jy
 * time:16-12-30上午11:20
 */
@Repository
public interface MqttMonitorDao {
    public List<MqttServerAddress> getServers();
}
