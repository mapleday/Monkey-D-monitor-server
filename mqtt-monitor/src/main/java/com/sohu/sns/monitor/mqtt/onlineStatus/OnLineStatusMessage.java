package com.sohu.sns.monitor.mqtt.onlineStatus;

/**
 * Created by jy on 16-8-30.
 * 用户在线状态维护消息
 */
public class OnLineStatusMessage {
    private String userId;
    private String cid;
    private String nodeId;
    private Integer type;
    private Long timeId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getTimeId() {
        return timeId;
    }

    public void setTimeId(Long timeId) {
        this.timeId = timeId;
    }

    @Override
    public String toString() {
        return "OnLineStatusMessage{" +
                "userId='" + userId + '\'' +
                ", cid='" + cid + '\'' +
                ", nodeId='" + nodeId + '\'' +
                ", type=" + type +
                ", timeId=" + timeId +
                '}';
    }
}
