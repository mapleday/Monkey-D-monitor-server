package com.sohu.sns.monitor.model;

/**
 * Created by Gary on 2015/12/15.
 */
public class StatLogInfo {

    private String appId;
    private String moduleName;
    private String methodName;
    private Long visitCount;
    private Long timeoutCount;
    private Double avgCompill;

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

    public Double getAvgCompill() {
        return avgCompill;
    }

    public void setAvgCompill(Double avgCompill) {
        this.avgCompill = avgCompill;
    }
}
