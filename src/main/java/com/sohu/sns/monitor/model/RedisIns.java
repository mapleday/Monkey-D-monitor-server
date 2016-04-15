package com.sohu.sns.monitor.model;

/**
 * Created by Gary Chan on 2016/4/15.
 */
public class RedisIns {

    private String ip;
    private Integer port;
    private Integer master;
    private Integer persistence;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getMaster() {
        return master;
    }

    public void setMaster(Integer master) {
        this.master = master;
    }

    public Integer getPersistence() {
        return persistence;
    }

    public void setPersistence(Integer persistence) {
        this.persistence = persistence;
    }
}
