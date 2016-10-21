package com.sohu.sns.monitor.model;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Gary Chan on 2016/4/12.
 */
public class UrlInstanceInfo {

    private AtomicLong consumeTime = new AtomicLong(0);
    private AtomicInteger exceptionNum = new AtomicInteger(0);

    public Long getConsumeTime() {
        return consumeTime.get();
    }

    public void addConsumeTime(int consumeTime) {
        this.consumeTime.addAndGet(consumeTime);
    }

    public Integer getExceptionNum() {
        return exceptionNum.get();
    }

    public void addExceptionNum(short exceptionNum) {
        this.exceptionNum.addAndGet(exceptionNum);
    }
}
