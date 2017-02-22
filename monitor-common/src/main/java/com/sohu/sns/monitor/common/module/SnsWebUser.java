package com.sohu.sns.monitor.common.module;

import java.io.Serializable;

/**
 * Created by yw on 2017/2/19.
 */
public class SnsWebUser implements Serializable{
    /**
     *   用户 id
     */
    private Integer id;

    /**
     *   用户真实姓名
     */
    private String name;

    /**
     *   用户角色
     */
    private String role;

    /**
     *  用户邮箱
     */
    private String email;

    /**
     *   用户登录名
     */
    private String loginName;

    /**
     *   用户登录密码
     */
    private String password;

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

    public String getRoleId() {
        return role;
    }

    public void setRoleId(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
