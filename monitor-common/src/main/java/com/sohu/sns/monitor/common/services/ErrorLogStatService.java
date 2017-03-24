package com.sohu.sns.monitor.common.services;

import com.sohu.sns.monitor.common.dao.errorLogStat.ErrorLogStatDao;
import com.sohu.sns.monitor.common.module.ErrorLogStat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by yw on 2017/3/23.
 */
@Service
public class ErrorLogStatService {
    @Autowired
    private ErrorLogStatDao errorLogStatDao;
    public List<ErrorLogStat> getAllErrorLogStats(){
        return errorLogStatDao.getAllErrorLogStats();
    }

    public void updatErrorLogStat(ErrorLogStat errorLogStat){
        errorLogStatDao.updateErrorLogStat(errorLogStat);
    }
}
