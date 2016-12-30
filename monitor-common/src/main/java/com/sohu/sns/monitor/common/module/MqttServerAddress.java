package com.sohu.sns.monitor.common.module;

import java.io.Serializable;

/**
 * author:jy
 * time:16-12-30上午11:20
 * mqtt服务器地址
 */
public class MqttServerAddress implements Serializable {
    private int id;
    private String serverAddress;
    private int monitorNum;
    private Integer stat;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public int getMonitorNum() {
        return monitorNum;
    }

    public void setMonitorNum(int monitorNum) {
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
