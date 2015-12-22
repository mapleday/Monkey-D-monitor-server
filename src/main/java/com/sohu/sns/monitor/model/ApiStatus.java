package com.sohu.sns.monitor.model;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Gary on 2015/11/6.
 */
public class ApiStatus {

    private String appId;
    private String moduleName;
    private String methodName;
    private AtomicLong useCount;
    private AtomicLong timeOutCount;

    public ApiStatus(){}
    public ApiStatus(String appId, String moduleName, String methodName, long useCount, long timeOutCount) {
        this.appId = appId;
        this.moduleName = moduleName;
        this.methodName = methodName;
        this.useCount = new AtomicLong(useCount);
        this.timeOutCount = new AtomicLong(timeOutCount);
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
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
