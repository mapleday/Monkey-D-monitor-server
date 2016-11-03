package com.sohu.sns.monitor.redis.model;

/**
 * Created by Gary Chan on 2016/4/22.
 */
public class MemoryInfo implements Comparable<MemoryInfo> {
    private String uid;
    private double maxMemory;
    private double usedMemory;
    private double lastUsedMemory;
    private double incr;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public double getMaxMemory() {
        return maxMemory;
    }

    public void setMaxMemory(double maxMemory) {
        this.maxMemory = maxMemory;
    }

    public double getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(double usedMemory) {
        this.usedMemory = usedMemory;
    }

    public double getLastUsedMemory() {
        return lastUsedMemory;
    }

    public void setLastUsedMemory(double lastUsedMemory) {
        this.lastUsedMemory = lastUsedMemory;
    }

    public double getIncr() {
        return incr;
    }

    public void setIncr(double incr) {
        this.incr = incr;
    }

    @Override
    public int compareTo(MemoryInfo o) {
        if(o.getIncr() > this.getIncr()) {
            return 1;
        } else if(o.getIncr() < this.getIncr()) {
            return -1;
        } else {
            return 0;
        }
    }
}
