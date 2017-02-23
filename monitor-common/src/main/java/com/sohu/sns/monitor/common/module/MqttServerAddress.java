package com.sohu.sns.monitor.common.module;

import java.io.Serializable;

/**
 * author:jy
 * time:16-12-30上午11:20
 * mqtt服务器地址
 */
public class MqttServerAddress implements Serializable {
    /**
     *   序号
     */
    private Integer id;

    /**
     *   服务器地址
     */
    private String serverAddress;

    /**
     *   监控次数
     */
    private Integer monitorNum;

    /**
     *   监控状态
     */
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
