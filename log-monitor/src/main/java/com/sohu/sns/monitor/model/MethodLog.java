package com.sohu.sns.monitor.model;

import java.io.Serializable;

/**
 * Created by morgan on 15/9/25.
 */
public class MethodLog implements Serializable {
    private Integer id;
    private String methodTraceId  ;
    private String className  ;
    private String methodName  ;
    private Integer consumeTime  ;
    private Long beginTime  ;
    private Long endTime  ;
    private String urlTraceId ;
    private String param;
    private String result ;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMethodTraceId() {
        return methodTraceId;
    }

    public void setMethodTraceId(String methodTraceId) {
        this.methodTraceId = methodTraceId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Integer getConsumeTime() {
        return consumeTime;
    }

    public void setConsumeTime(Integer consumeTime) {
        this.consumeTime = consumeTime;
    }

    public Long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Long beginTime) {
        this.beginTime = beginTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public String getUrlTraceId() {
        return urlTraceId;
    }

    public void setUrlTraceId(String urlTraceId) {
        this.urlTraceId = urlTraceId;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
