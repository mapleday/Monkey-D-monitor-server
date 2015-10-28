package com.sohu.sns.monitor.model;

/**
 * Created by Gary on 2015/10/28.
 */
public class MergedErrorLog {
    private ErrorLog errorLog;
    private StringBuffer params = new StringBuffer();
    private int times = 0;

    public ErrorLog getErrorLog() {
        return errorLog;
    }

    public void setErrorLog(ErrorLog errorLog) {
        this.errorLog = errorLog;
    }

    public StringBuffer getParams() {
        return params;
    }

    public void addParams(String param) {
        this.params.append(" || "+param);
    }

    public int getTimes() {
        return times;
    }

    public void addTimes(int times) {
        this.times += times;
    }
}
