package com.sohu.sns.monitor.common.module;

import java.io.Serializable;

/**
 * author:jy
 * time:16-10-13下午3:30
 * http资源配置
 *
 * author: yw on 2017/2/8.
 */
public class HttpResource implements Serializable {
    /**
     * 资源ID
     */
    private Integer id;
    /**
     * 资源名称
     */
    private String resourceName;
    /**
     * 资源地址
     */
    private String resourceAddress;
    /**
     * 监控资源超时时间
     */
    private Integer monitorTimeOut;
    /**
     * 监控间隔
     */
    private Integer monitorInterval;
    /**
     * 监控次数
     */
    private Integer monitorTimes;
    /**
     * 报警阀值
     */
    private Integer alarmThresholdTimes;
    /**
     * 监控状态
     */
    private Integer stat;

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getResourceAddress() {
        return resourceAddress;
    }

    public void setResourceAddress(String resourceAddress) {
        this.resourceAddress = resourceAddress;
    }

    public Integer getMonitorTimeOut() {
        return monitorTimeOut;
    }

    public void setMonitorTimeOut(Integer monitorTimeOut) {
        this.monitorTimeOut = monitorTimeOut;
    }

    public Integer getMonitorInterval() {
        return monitorInterval;
    }

    public void setMonitorInterval(Integer monitorInterval) {
        this.monitorInterval = monitorInterval;
    }

    public Integer getMonitorTimes() {
        return monitorTimes;
    }

    public void setMonitorTimes(Integer monitorTimes) {
        this.monitorTimes = monitorTimes;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAlarmThresholdTimes() {
        return alarmThresholdTimes;
    }

    public void setAlarmThresholdTimes(Integer alarmThresholdTimes) {
        this.alarmThresholdTimes = alarmThresholdTimes;
    }

    public Integer getStat() {
        return stat;
    }

    public void setStat(Integer stat) {
        this.stat = stat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HttpResource that = (HttpResource) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "HttpResource{" +
                "id=" + id +
                ", resourceName='" + resourceName + '\'' +
                ", resourceAddress='" + resourceAddress + '\'' +
                ", monitorTimeOut=" + monitorTimeOut +
                ", monitorInterval=" + monitorInterval +
                ", monitorTimes=" + monitorTimes +
                ", alarmThresholdTimes=" + alarmThresholdTimes +",stat="+stat+
                '}';
    }
}
