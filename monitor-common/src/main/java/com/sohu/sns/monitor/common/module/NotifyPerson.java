package com.sohu.sns.monitor.common.module;

import java.io.Serializable;

/**
 * author:jy
 * time:16-12-28下午3:11
 *
 * update by yw on 17.2.24
 * 通知人
 */
public class NotifyPerson implements Serializable {
    /**
     * 自增主键
     */
    private Integer id;
    private String name;
    private String email;
    private String phone;
    private Integer status;
    private String group;
    private Integer waitDutyStatus;
    private Integer dutyIngroup;

    public Integer getDutyIngroup() {
        return dutyIngroup;
    }

    public void setDutyIngroup(Integer dutyIngroup) {
        this.dutyIngroup = dutyIngroup;
    }

    public Integer getWaitDutyStatus() {
        return waitDutyStatus;
    }

    public void setWaitDutyStatus(Integer waitDutyStatus) {
        this.waitDutyStatus = waitDutyStatus;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }


    @Override
    public String toString() {
        return "NotifyPerson{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", status=" + status +
                ", group='" + group + '\'' +
                ", waitDutyStatus=" + waitDutyStatus +
                ", dutyIngroup=" + dutyIngroup +
                '}';
    }
}
