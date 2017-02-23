package com.sohu.sns.monitor.common.dao.snsWebUser;

import com.sohu.sns.monitor.common.module.SnsWebUser;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * Created by yw on 2017/2/20.
 */

@Repository
public interface SnsWebUserDao {
    /**
     *  根据id查用户信息
     * @param loginName
     * @return
     */
    public List<SnsWebUser> getUser(String loginName);


    /**
     * 注册新用户
     * @param snsWebUser
     */
    public void createUser(SnsWebUser snsWebUser);


    /**
     *  查询用户的角色
     */
    public Set<String> getRole(String loginName);
}
