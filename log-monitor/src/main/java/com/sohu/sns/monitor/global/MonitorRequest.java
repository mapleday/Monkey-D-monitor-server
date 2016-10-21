package com.sohu.sns.monitor.global;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * sns-api的请求
 * Created by morgan on 15/4/17.
 */
public class MonitorRequest {

    private final String path;
    private final Set<String> methodSet = new HashSet<String>();
    private final Set<String> requiredSet = new HashSet<String>();
    private final Method method;
    private final boolean isCheckToken;
    //2015-12-14 18:51:45 是否检测重放
    private final boolean isCheckReplay;

    public MonitorRequest(String path, String[] methods, String[] requireds, Method method, boolean isCheckToken, boolean isCheckReplay) {
        this.path = path;
        this.isCheckToken = isCheckToken;
        this.isCheckReplay = isCheckReplay;
        if(methodSet != null && methods.length > 0){
            for (int i = 0; i < methods.length; i++) {
                this.methodSet.add(methods[i].toLowerCase());
            }
        }
        if (requireds != null && requireds.length > 0) {
            for (int i = 0; i < requireds.length; i++) {
                this.requiredSet.add(requireds[i]);
            }
        }
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public Set<String> getMethodSet() {
        return methodSet;
    }

    public Set<String> getRequiredSet() {
        return requiredSet;
    }

    public Method getMethod() {
        return method;
    }

    public boolean isCheckToken() {
        return isCheckToken;
    }

    public boolean isCheckReplay() {
        return isCheckReplay;
    }

    @Override
    public String toString() {
        return "MonitorRequest{" +
                "path='" + path + '\'' +
                ", methodSet=" + methodSet +
                ", requiredSet=" + requiredSet +
                ", method=" + method +
                ",isCheckToken=" +isCheckToken +
                '}';
    }
}
