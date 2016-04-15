package com.sohu.sns.monitor.model;

/**
 * Created by Gary Chan on 2016/4/15.
 */
public class RedisInfo {

    private String ip = "";
    private Integer isMaster = 0;
    private Long keys = 0L;
    private Long maxMemory = 0L;
    private Long usedMemory = 0L;
    private String usedCpu = "";
    private Long connectedClients = 0L;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getIsMaster() {
        return isMaster;
    }

    public void setIsMaster(Integer isMaster) {
        this.isMaster = isMaster;
    }

    public Long getKeys() {
        return keys;
    }

    public void setKeys(Long keys) {
        this.keys = keys;
    }

    public Long getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(Long maxMemory) {
        this.maxMemory = maxMemory;
    }

    public Long getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(Long usedMemory) {
        this.usedMemory = usedMemory;
    }

    public String getUsedCpu() {
        return usedCpu;
    }

    public void setUsedCpu(String usedCpu) {
        this.usedCpu = usedCpu;
    }

    public Long getConnectedClients() {
        return connectedClients;
    }

    public void setConnectedClients(Long connectedClients) {
        this.connectedClients = connectedClients;
    }
}
