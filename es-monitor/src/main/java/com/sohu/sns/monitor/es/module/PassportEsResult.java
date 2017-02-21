package com.sohu.sns.monitor.es.module;

/**
 * Created by morgan on 2017/2/20.
 */
public class PassportEsResult {

    private String interfaceUri;
    private Integer count;
    private Integer lastCount;
    private Integer totalCount;
    private Integer lastTotalCount;
    private String timeKey;
    private String minKey;
    private String color = "#FFFFFF";

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getInterfaceUri() {
        return interfaceUri;
    }

    public void setInterfaceUri(String interfaceUri) {
        this.interfaceUri = interfaceUri;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getLastCount() {
        return lastCount;
    }

    public void setLastCount(Integer lastCount) {
        this.lastCount = lastCount;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getLastTotalCount() {
        return lastTotalCount;
    }

    public void setLastTotalCount(Integer lastTotalCount) {
        this.lastTotalCount = lastTotalCount;
    }

    public String getTimeKey() {
        return timeKey;
    }

    public void setTimeKey(String timeKey) {
        this.timeKey = timeKey;
    }

    public String getMinKey() {
        return minKey;
    }

    public void setMinKey(String minKey) {
        this.minKey = minKey;
    }

    public boolean isSameMinute(PassportEsResult obj) {
        if (this.minKey == null || obj.minKey == null) {
            return false;
        }
        return this.minKey.equals(obj.minKey);
    }

    @Override
    public String toString() {
        return "PassportEsResult{" +
                "interfaceUri='" + interfaceUri + '\'' +
                ", count=" + count +
                ", lastCount=" + lastCount +
                ", totalCount=" + totalCount +
                ", lastTotalCount=" + lastTotalCount +
                ", timeKey='" + timeKey + '\'' +
                ", minKey='" + minKey + '\'' +
                '}';
    }
}
