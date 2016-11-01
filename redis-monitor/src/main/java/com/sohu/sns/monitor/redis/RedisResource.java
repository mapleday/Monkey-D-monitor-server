package com.sohu.sns.monitor.redis;

/**
 * author:jy
 * time:16-10-31下午3:52
 * redis资源
 */
public class RedisResource {
    /**
     * uid
     */
    private int uid;
    /**
     * 查看redis配置的地址
     */
    private String apiUrl;
    /**
     * redis密码
     */
    private String passwd;
    /**
     * 资源描述
     */
    private String desc;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "RedisResource{" +
                "uid=" + uid +
                ", apiUrl='" + apiUrl + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }
}
