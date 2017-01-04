package com.sohu.sns.monitor.common.dao.timeoutApiCollect;/**
 * 功能描述：
 * <p>
 * Created by jy on 17-1-4.
 */

import com.sohu.sns.monitor.common.module.TimeoutApiCollect;
import org.springframework.stereotype.Repository;

/**
 * author:jy
 * time:17-1-4下午4:27
 */
@Repository
public interface TimeoutApiCollectDao {
    /**
     * 获取记录数
     *
     * @param collect
     * @return
     */
    public int getTimeOutCount(TimeoutApiCollect collect);

    /**
     * 保存记录
     *
     * @param collect
     */
    public void saveTimeOut(TimeoutApiCollect collect);

    /**
     * 更新记录
     *
     * @param collect
     */
    public void updateTimeOutCount(TimeoutApiCollect collect);

}
