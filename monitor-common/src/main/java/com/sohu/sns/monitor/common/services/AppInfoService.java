package com.sohu.sns.monitor.common.services;

import com.sohu.sns.monitor.common.dao.appInfo.AppInfoDao;
import com.sohu.sns.monitor.common.module.AppInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by yw on 2017/3/26.
 */
@Service
public class AppInfoService {
    @Autowired
    private AppInfoDao appInfoDao;
    public List<AppInfo> getAppInfo(String appId){
        return appInfoDao.getAppInfo(appId);
    }
}
