package com.sohu.sns.monitor.es.module;

import java.io.Serializable;

/**
 * Created by lc on 17-2-24.
 */
public class Message implements Comparable<Message>,Serializable{

    private String messageName;
    private long count;
    private double avgTime;

    public Message() {
    }

    public Message(String messageName, Long count, double avgTime) {
        this.messageName = messageName;
        this.count = count;
        this.avgTime = avgTime;
    }

    public String getMessageName() {
        return messageName;
    }

    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public double getAvgTime() {
        return avgTime;
    }

    public void setAvgTime(double avgTime) {
        this.avgTime = avgTime;
    }


    @Override
    public int compareTo(Message o) {
        return (int) ((o.getCount()-this.getCount()+Math.random()/1000) * 10000);
    }
}
