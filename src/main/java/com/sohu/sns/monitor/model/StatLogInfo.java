package com.sohu.sns.monitor.model;

/**
 * Created by Gary on 2015/12/15.
 */
public class StatLogInfo {
    private String appId;
    private String instanceId;
    private String moduleName;
    private String methodName;
    private int instanceNum;
    private Long visitCount;
    private Long timeoutCount;
    private Long allCompileTime;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
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

    public int getInstanceNum() {
        return instanceNum;
    }

    public void setInstanceNum(int instanceNum) {
        this.instanceNum = instanceNum;
    }

    public Long getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(Long visitCount) {
        this.visitCount = visitCount;
    }

    public Long getTimeoutCount() {
        return timeoutCount;
    }

    public void setTimeoutCount(Long timeoutCount) {
        this.timeoutCount = timeoutCount;
    }

    public Long getAllCompileTime() {
        return allCompileTime;
    }

    public void setAllCompileTime(Long allCompileTime) {
        this.allCompileTime = allCompileTime;
    }
}
