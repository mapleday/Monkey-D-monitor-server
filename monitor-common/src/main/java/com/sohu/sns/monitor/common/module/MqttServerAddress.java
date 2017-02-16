package com.sohu.sns.monitor.common.module;

import java.io.Serializable;

/**
 * author:jy
 * time:16-12-30上午11:20
 * mqtt服务器地址
 */
public class MqttServerAddress implements Serializable {
    private Integer id;
    private String serverAddress;
    private Integer monitorNum;
    private Integer stat;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public Integer getMonitorNum() {
        return monitorNum;
    }

    public void setMonitorNum(Integer monitorNum) {
        this.monitorNum = monitorNum;
    }

    public Integer getStat() {
        return stat;
    }

    public void setStat(Integer stat) {
        this.stat = stat;
    }

    @Override
    public String toString() {
        return "MqttServerAddress{" +
                "id=" + id +
                ", serverAddress='" + serverAddress + '\'' +
                ", monitorNum=" + monitorNum +
                ", stat=" + stat +
                '}';
    }
}
