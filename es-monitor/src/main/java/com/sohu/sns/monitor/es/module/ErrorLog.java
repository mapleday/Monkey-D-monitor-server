package com.sohu.sns.monitor.es.module;

import java.io.Serializable;

/**
 * Created by yw on 2017/3/16.
 */
public class ErrorLog implements Serializable{
    private String exceptionName;
    private String module;
    private String appId;
    private String param;
    private String returnValue;
    private String method;
    private String exceptionDesc;
    private String stackTrace;
    private String version;
    private String timestamp;

    @Override
    public String toString() {
        return "ErrorLog{" +
                "exceptionName='" + exceptionName + '\'' +
                ", module='" + module + '\'' +
                ", appId='" + appId + '\'' +
                ", param='" + param + '\'' +
                ", returnValue='" + returnValue + '\'' +
                ", method='" + method + '\'' +
                ", exceptionDesc='" + exceptionDesc + '\'' +
                ", stackTrace='" + stackTrace + '\'' +
                ", version='" + version + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }



    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getExceptionName() {
        return exceptionName;
    }

    public void setExceptionName(String exceptionName) {
        this.exceptionName = exceptionName;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(String returnValue) {
        this.returnValue = returnValue;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getExceptionDesc() {
        return exceptionDesc;
    }

    public void setExceptionDesc(String exceptionDesc) {
        this.exceptionDesc = exceptionDesc;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

}
