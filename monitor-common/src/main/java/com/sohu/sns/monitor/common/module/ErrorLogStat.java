package com.sohu.sns.monitor.common.module;

import java.io.Serializable;

/**
 * Created by yw on 2017/3/22.
 */
public class ErrorLogStat implements Serializable {
    private String appId;
    private Integer errorCount;
    private Integer threshold ;
    private String instanceId;
    private String appName;
    private String appDeveloper;
    private String exceptionName;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public Integer getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Integer errorCount) {
        this.errorCount = errorCount;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppDeveloper() {
        return appDeveloper;
    }

    public void setAppDeveloper(String appDeveloper) {
        this.appDeveloper = appDeveloper;
    }

    public String getExceptionName() {
        return exceptionName;
    }

    public void setExceptionName(String exceptionName) {
        this.exceptionName = exceptionName;
    }

    @Override
    public String toString() {
        return "ErrorLogStat{" +
                "appId='" + appId + '\'' +
                ", errorCount=" + errorCount +
                ", threshold=" + threshold +
                ", instanceId='" + instanceId + '\'' +
                ", appName='" + appName + '\'' +
                ", appDeveloper='" + appDeveloper + '\'' +
                ", exceptionName='" + exceptionName + '\'' +
                '}';
    }
}
