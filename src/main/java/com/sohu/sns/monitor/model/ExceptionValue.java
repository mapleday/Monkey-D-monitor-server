package com.sohu.sns.monitor.model;

/**
 * Created by Gary on 2016/1/18.
 */
public class ExceptionValue {
    private String appId;
    private String moduleName;
    private String methodName;
    private Integer visitCount;
    private Integer maxVisitCount;
    private Integer minVisitCount;
    private Integer avgVisitCount;

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

    public Integer getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(Integer visitCount) {
        this.visitCount = visitCount;
    }

    public Integer getMaxVisitCount() {
        return maxVisitCount;
    }

    public void setMaxVisitCount(Integer maxVisitCount) {
        this.maxVisitCount = maxVisitCount;
    }

    public Integer getMinVisitCount() {
        return minVisitCount;
    }

    public void setMinVisitCount(Integer minVisitCount) {
        this.minVisitCount = minVisitCount;
    }

    public Integer getAvgVisitCount() {
        return avgVisitCount;
    }

    public void setAvgVisitCount(Integer avgVisitCount) {
        this.avgVisitCount = avgVisitCount;
    }
}
