package com.sohu.sns.monitor.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Gary on 2015/11/6.
 */
public class ApiStatus implements Serializable{

    private String moduleName;
    private String methodName;
    private String param;
    private String returnValue;
    private Long compMill;
    private Long cacheMill;
    private Long thirdIterMill;
    private Date date;

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

    public Long getCompMill() {
        return compMill;
    }

    public void setCompMill(Long compMill) {
        this.compMill = compMill;
    }

    public Long getCacheMill() {
        return cacheMill;
    }

    public void setCacheMill(Long cacheMill) {
        this.cacheMill = cacheMill;
    }

    public Long getThirdIterMill() {
        return thirdIterMill;
    }

    public void setThirdIterMill(Long thirdIterMill) {
        this.thirdIterMill = thirdIterMill;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
