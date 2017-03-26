package com.sohu.sns.monitor.common.module;

import java.io.Serializable;

/**
 * Created by yw on 2017/3/26.
 */
public class AppInfo implements Serializable{
    private String appId;
    private String appName;
    private String appDeveloper;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
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

    @Override
    public String toString() {
        return "AppInfo{" +
                "appId=" + appId +
                ", appName='" + appName + '\'' +
                ", appDeveloper='" + appDeveloper + '\'' +
                '}';
    }
}
