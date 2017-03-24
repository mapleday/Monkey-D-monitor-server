package com.sohu.sns.monitor.common.module;

import java.io.Serializable;

/**
 * Created by yw on 2017/3/22.
 */
public class ErrorLogStat implements Serializable {
    private String appId;
    private Integer errorCount;
    private Integer threshold ;

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
}
