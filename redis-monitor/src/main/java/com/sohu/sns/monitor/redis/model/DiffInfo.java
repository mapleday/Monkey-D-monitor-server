package com.sohu.sns.monitor.redis.model;

import java.util.List;

/**
 * Created by Gary Chan on 2016/4/22.
 */
public class DiffInfo implements Comparable<DiffInfo>{
    private String uid;
    private List<RedisInfo> list;
    private Integer maxDiff;
    private Integer diffByLast;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public List<RedisInfo> getList() {
        return list;
    }

    public void setList(List<RedisInfo> list) {
        this.list = list;
    }

    public Integer getMaxDiff() {
        return maxDiff;
    }

    public void setMaxDiff(Integer maxDiff) {
        this.maxDiff = maxDiff;
    }

    public Integer getDiffByLast() {
        return diffByLast;
    }

    public void setDiffByLast(Integer diffByLast) {
        this.diffByLast = diffByLast;
    }

    @Override
    public int compareTo(DiffInfo o) {
        if(o.getDiffByLast() > this.getDiffByLast()) {
            return 1;
        } else if(o.getDiffByLast() < this.getDiffByLast()) {
            return -1;
        } else {
            return 0;
        }
    }
}
