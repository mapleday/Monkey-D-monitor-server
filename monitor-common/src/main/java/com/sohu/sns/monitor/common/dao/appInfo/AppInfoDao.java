package com.sohu.sns.monitor.common.dao.appInfo;

import com.sohu.sns.monitor.common.module.AppInfo;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by yw on 2017/3/26.
 */
@Repository
public interface AppInfoDao {
    public List<AppInfo> getAppInfo(String appId);

}
