package com.sohu.sns.monitor.httpmonitor;

/**
 * author:jy
 * time:16-10-13下午3:57
 * 监控结果
 */
public class MonitorResult {
    /**
     * 失败状态
     */
    private boolean failed;
    /**
     * 资源
     */
    private String resouceAddress;
    /**
     * 失败原因
     */
    private String failedReason;
    /**
     * 失败次数
     */
    private int failedTimes;
    /**
     * 监控次数
     */
    private int monitorTimes;


    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public String getFailedReason() {
        return failedReason;
    }

    public void setFailedReason(String failedReason) {
        this.failedReason = failedReason;
    }

    public String getResouceAddress() {
        return resouceAddress;
    }

    public void setResouceAddress(String resouceAddress) {
        this.resouceAddress = resouceAddress;
    }

    public Integer getFailedTimes() {
        return failedTimes;
    }

    public void setFailedTimes(Integer failedTimes) {
        this.failedTimes = failedTimes;
    }

    public void addFailedTimes() {
        failedTimes++;
    }

    public void setFailedTimes(int failedTimes) {
        this.failedTimes = failedTimes;
    }

    public int getMonitorTimes() {
        return monitorTimes;
    }

    public void setMonitorTimes(int monitorTimes) {
        this.monitorTimes = monitorTimes;
    }

    @Override
    public String toString() {
        return "MonitorResult{" +
                "failed=" + failed +
                ", resouceAddress='" + resouceAddress + '\'' +
                ", failedReason='" + failedReason + '\'' +
                ", failedTimes=" + failedTimes +
                ", monitorTimes=" + monitorTimes +
                '}';
    }
}
