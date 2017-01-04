package com.sohu.sns.monitor.common.module;

import java.util.Date;

/**
 * author:jy
 * time:17-1-4下午4:47
 * 错误统计
 * 这个不仅仅是超时，是所有的错误统计
 */
public class TimeoutApiCollect {
    private Integer id;
    private String appId;
    private String moduleName;
    private String methodName;
    private Integer timeoutCount;
    private String dateStr;
    private Date updateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Integer getTimeoutCount() {
        return timeoutCount;
    }

    public void setTimeoutCount(Integer timeoutCount) {
        this.timeoutCount = timeoutCount;
    }

    public String getDateStr() {
        return dateStr;
    }

    public void setDateStr(String dateStr) {
        this.dateStr = dateStr;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
