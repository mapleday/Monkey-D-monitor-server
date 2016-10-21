package com.sohu.sns.monitor.agent.store.model;

import java.util.List;

/**
 * Created by carvin on 2015/2/1.
 */
public class Applications {
    private List<Application> applicationList;

    public Applications() {}
    public Applications(List<Application> applicationList) {
        this.applicationList = applicationList;
    }
    public List<Application> getApplicationList() {
        return applicationList;
    }

    public void setApplicationList(List<Application> applicationList) {
        this.applicationList = applicationList;
    }
}
