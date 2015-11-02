package com.sohu.sns.monitor.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Gary on 2015/10/19.
 */
public class ErrorLog implements Serializable {
    private String appId;
    private String instanceId;
    private String module;
    private String method;
    private String param;
    private String returnValue;
    private String exceptionName;
    private String exceptionDesc;
    private Date time;

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

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
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

    public String getExceptionName() {
        return exceptionName;
    }

    public void setExceptionName(String exceptionName) {
        this.exceptionName = exceptionName;
    }

    public String getExceptionDesc() {
        return exceptionDesc;
    }

    public void setExceptionDesc(String exceptionDesc) {
        this.exceptionDesc = exceptionDesc;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getKey() {
        return getMethod() + getMethod() + getExceptionName();
    }

    public String warpHtml() {
        return "<tr><td align=\"center\" width=\"120\"><b>Module</b></td><td style=\"word-wrap:break-word;\"><b>"+this.getModule()+"</b></td></tr>" +
                "<tr><td align=\"center\"><b>Method</b></td><td style=\"word-wrap:break-word;\">"+this.getMethod()+"</td></tr>" +
                "<tr><td align=\"center\"><b>returnValue</b></td><td style=\"word-wrap:break-word;\">"+this.getReturnValue()+"</td></tr>" +
                "<tr><td align=\"center\"><b>exceptionName</b></td><td style=\"word-wrap:break-word;\">"+this.getExceptionName()+"</td></tr>" +
                "<tr><td align=\"center\" ><b>exceptionDesc</b></td><td style=\"word-wrap:break-word;\">"+this.getExceptionDesc()+"</td></tr>" +
                "<tr><td align=\"center\"><b>Occur_Time</font></b></td><td style=\"word-wrap:break-word;\">"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(this.getTime())+"</td></tr>";
    }

}
