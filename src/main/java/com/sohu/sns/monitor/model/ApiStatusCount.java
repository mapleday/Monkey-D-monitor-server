package com.sohu.sns.monitor.model;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Gary on 2015/11/6.
 */
public class ApiStatusCount {

    private String methodName;
    private String pathName;
    private AtomicLong useCount;
    private AtomicLong timeOutCount;

    public ApiStatusCount(){}
    public ApiStatusCount(String methodName, long useCount, long timeOutCount) {
        this.methodName = methodName;
        this.useCount = new AtomicLong(useCount);
        this.timeOutCount = new AtomicLong(timeOutCount);
    }

    public String getPathName() {
        return pathName;
    }

    public ApiStatusCount setPathName(String pathName) {
        this.pathName = pathName;
        return this;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Long getUseCount() {
        return this.useCount.get();
    }

    public Long getTimeOutCount() {
        return this.timeOutCount.get();
    }

    public void addUseCount(long time) {
        this.useCount.addAndGet(time);
    }

    public void addTimeOutCount(long time) {
        this.timeOutCount.addAndGet(time);
    }
}
