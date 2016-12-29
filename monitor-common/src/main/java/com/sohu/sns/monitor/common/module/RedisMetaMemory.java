package com.sohu.sns.monitor.common.module;

import java.util.Date;

/**
 * author:jy
 * time:16-12-29下午5:15
 * meta redis 使用的内存情况
 */
public class RedisMetaMemory {
    private Integer id;
    private Double lastDayUserMemory;
    private Double usedMemory;
    private String logDay;
    private Date updateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getLastDayUserMemory() {
        return lastDayUserMemory;
    }

    public void setLastDayUserMemory(Double lastDayUserMemory) {
        this.lastDayUserMemory = lastDayUserMemory;
    }

    public Double getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(Double usedMemory) {
        this.usedMemory = usedMemory;
    }

    public String getLogDay() {
        return logDay;
    }

    public void setLogDay(String logDay) {
        this.logDay = logDay;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "RedisMetaMemory{" +
                "id=" + id +
                ", lastDayUserMemory=" + lastDayUserMemory +
                ", usedMemory=" + usedMemory +
                ", logDay='" + logDay + '\'' +
                ", updateTime=" + updateTime +
                '}';
    }
}
