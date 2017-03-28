package com.sohu.sns.monitor.common.dao.errorLogStat;

import com.sohu.sns.monitor.common.module.ErrorLogStat;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by yw on 2017/3/23.
 */
@Repository
public interface ErrorLogStatDao {
    public List<ErrorLogStat> getErrorLogStats();
    public List<ErrorLogStat> getErrorLogStatById(ErrorLogStat errorLogStat);
    public void updateErrorLogStat(ErrorLogStat errorLogStat);
    public void insertErrorLogStat(ErrorLogStat errorLogStat);




}
