package com.sohu.sns.monitor.common.services;

import com.sohu.sns.monitor.common.dao.snsWebUser.SnsWebUserDao;
import com.sohu.sns.monitor.common.module.SnsWebUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Created by yw on 2017/2/20.
 */

@Component
public class SnsWebUserService {
    @Autowired
    SnsWebUserDao snsWebUserDao;

    public List<SnsWebUser> getSnsWebUser(String loginName) {
        return snsWebUserDao.getUser(loginName);
    }

    public void createUser(SnsWebUser snsWebUser){
        snsWebUserDao.createUser(snsWebUser);
    }

    public Set<String> getRole(String loginName){
        return snsWebUserDao.getRole(loginName);
    }
}
