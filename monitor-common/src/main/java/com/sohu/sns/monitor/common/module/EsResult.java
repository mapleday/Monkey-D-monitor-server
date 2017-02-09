package com.sohu.sns.monitor.common.module;

import java.io.Serializable;
import java.util.Date;

/**
 * author:jy
 * time:17-1-18下午5:59
 * es查询结果
 */
public class EsResult implements Comparable<EsResult> , Serializable {
    private String interfaceUri;
    private double avgTime;
    private long totoalCount;
    private double qps;
    private String updateTime;

    public String getInterfaceUri() {
        return interfaceUri;
    }

    public void setInterfaceUri(String interfaceUri) {
        this.interfaceUri = interfaceUri;
    }

    public double getAvgTime() {
        return avgTime;
    }

    public void setAvgTime(double avgTime) {
        this.avgTime = avgTime;
    }

    public long getTotoalCount() {
        return totoalCount;
    }

    public void setTotoalCount(long totoalCount) {
        this.totoalCount = totoalCount;
    }

    public double getQps() {
        return qps;
    }

    public void setQps(double qps) {
        this.qps = qps;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }



    @Override
    public String toString() {
        return "EsResult{" +
                "interfaceUri='" + interfaceUri + '\'' +
                ", avgTime=" + avgTime +
                ", totoalCount=" + totoalCount +
                ", qps=" + qps +
                '}';
    }


    @Override
    public int compareTo(EsResult o) {
        return (int) ((o.getQps() - this.getQps()) * 100000);
    }
}
