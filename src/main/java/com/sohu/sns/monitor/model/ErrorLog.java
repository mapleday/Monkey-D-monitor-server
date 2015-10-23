package com.sohu.sns.monitor.model;

import java.io.Serializable;

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

    @Override
    public String toString() {
        /*return "【Module】 : "+ this.getModule() + ", 【Method】 : " + this.getMethod() + ", 【Param】 : " +
                this.getParam() + ", 【returnValue】 : " + this.getReturnValue() + ", 【ExceptionName】 : " +
                this.getExceptionName() + ", 【ExceptionDesc】 : " + this.getExceptionDesc();*/
        return "<tr><th><b><font color='red'>Module</font></b></th><th>"+this.getModule()+"</th></tr>" +
                "<tr><td><b><font color='red'>Method</font></b></td><td>"+this.getMethod()+"</td></tr>" +
                "<tr><td><b><font color='red'>Param</font></b></td><td>"+this.getParam()+"</td></tr>" +
                "<tr><td><b><font color='red'>returnValue</font></b></td><td>"+this.getReturnValue()+"</td></tr>" +
                "<tr><td><b><font color='red'>exceptionName</font></b></td><td>"+this.getExceptionName()+"</td></tr>" +
                "<tr><td><b><font color='red'>exceptionDesc</font></b></td><td>"+this.getExceptionDesc()+"</td></tr>";
    }
}
