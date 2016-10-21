package com.sohu.sns.monitor.model;

import java.io.Serializable;

/**
 * Created by morgan on 15/9/24.
 */
public class MonitorUrl implements Serializable {

    private Integer id ;
    private String urlTraceId ;
    private String url ;
    private String appId  ;
    private String instanceId ;
    private Integer consumeTime ;
    private Long beginTime ;
    private Long endTime ;
    private Integer methodCount;
    private String params;
    private Integer resultLength;
    private Short hasException ;
    private String headers ;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUrlTraceId() {
        return urlTraceId;
    }

    public void setUrlTraceId(String urlTraceId) {
        this.urlTraceId = urlTraceId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

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

    public Integer getMethodCount() {
        return methodCount;
    }

    public void setMethodCount(Integer methodCount) {
        this.methodCount = methodCount;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public Integer getResultLength() {
        return resultLength;
    }

    public void setResultLength(Integer resultLength) {
        this.resultLength = resultLength;
    }

    public Short getHasException() {
        return hasException;
    }

    public void setHasException(Short hasException) {
        this.hasException = hasException;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }
}
