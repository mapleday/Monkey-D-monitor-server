package com.sohu.sns.monitor.common.services;

import com.sohu.sns.monitor.common.dao.timeoutApiCollect.TimeoutApiCollectDao;
import com.sohu.sns.monitor.common.module.TimeoutApiCollect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/1/9.
 */

@Component
public class TimeoutApiCollectService {
    @Autowired
    TimeoutApiCollectDao timeoutApiCollectDao;

    /**
     * 添加记录
     * @param timeoutApiCollect
     */
    public void addTimeoutApiCollect(TimeoutApiCollect timeoutApiCollect){
        timeoutApiCollectDao.saveTimeOut(timeoutApiCollect);
    }

    /**
     * 修改记录
     * @param timeoutApiCollect
     */
    public void updateTimeoutApiCollct(TimeoutApiCollect timeoutApiCollect){
        timeoutApiCollectDao.updateTimeOutCount(timeoutApiCollect);
    }

    /**
     * 删除记录
     * @param timeoutApiCollect
     */
    public void deleteTimeoutApiCollect(TimeoutApiCollect timeoutApiCollect){
        timeoutApiCollectDao.deleteTimeOutCount(timeoutApiCollect);
    }

}
