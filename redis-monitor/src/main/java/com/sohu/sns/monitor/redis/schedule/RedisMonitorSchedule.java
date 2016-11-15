package com.sohu.sns.monitor.redis.schedule;

import com.sohu.sns.monitor.redis.timer.RedisDataCheckProfessor;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by yzh on 2016/11/3.
 * redis监控
 */
@Component
public class RedisMonitorSchedule {
    @Autowired
    private RedisDataCheckProfessor professor;

    @Scheduled(fixedRate = 3600000L, initialDelay = 3600000L)
    public void checkRedisAndSendMail() {
        try {
            LOGGER.buziLog(ModuleEnum.MONITOR_SERVICE, "RedisMonitorSchedule.checkRedisAndSendMail", "准备发邮件...", "");
            professor.handle(0);
            LOGGER.buziLog(ModuleEnum.MONITOR_SERVICE, "RedisMonitorSchedule.checkRedisAndSendMail", "发邮件完成...", "");
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "RedisMonitorSchedule.checkRedisAndSendMail", null, null, e);
        }
    }

    @Scheduled(fixedRate = 60000L, initialDelay = 60000L)
    public void checkRedisAndSendWeixin() {
        try {
            LOGGER.buziLog(ModuleEnum.MONITOR_SERVICE, "RedisMonitorSchedule.checkRedisAndSendWeixin", "准备发微信...", "");
            professor.handle(1);
            LOGGER.buziLog(ModuleEnum.MONITOR_SERVICE, "RedisMonitorSchedule.checkRedisAndSendWeixin", "发微信完成...", "");
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "RedisMonitorSchedule.checkRedisAndSendWeixin", null, null, e);
        }
    }

}
