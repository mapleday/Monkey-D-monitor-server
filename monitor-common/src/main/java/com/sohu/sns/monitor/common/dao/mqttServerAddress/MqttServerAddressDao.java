package com.sohu.sns.monitor.common.dao.mqttServerAddress;

import org.springframework.stereotype.Repository;
import com.sohu.sns.monitor.common.module.MqttServerAddress;

import java.util.List;

/**
 * author:yw on 2017.2.9
 *
 */
@Repository
public interface MqttServerAddressDao {

    public List<MqttServerAddress> getMqtt();
    public void updateMqtt(MqttServerAddress msa);
    public void deleteMqtt(MqttServerAddress msa);
    public void createMqtt(MqttServerAddress msa);

}
