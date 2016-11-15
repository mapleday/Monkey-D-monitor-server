package com.sohu.sns.monitor.redis.schedule;

import com.sohu.sns.monitor.redis.config.ZkPathConfig;
import com.sohu.sns.monitor.redis.timer.RedisDataCheckProfessor;
import com.sohu.sns.monitor.redis.util.MysqlClusterServiceUtils;
import com.sohu.sns.monitor.redis.util.ZkLockUtil;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.config.ZkPathConfigure;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.exception.SnsConfigException;
import com.sohu.snscommon.utils.zk.SnsDiamonds;
import com.sohu.snscommon.utils.zk.ZkUtils;
import org.apache.zookeeper.KeeperException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Created by yzh on 2016/11/3.
 */
@Component
public class RedisMonitorSchedule {
    private static RedisDataCheckProfessor professor = new RedisDataCheckProfessor();

    @Scheduled(fixedRate = 3600000L, initialDelay = 3600000L)
    public void checkRedisAndSendMail() {
        try {
            System.out.println("\n 准备发邮件...");
            professor.handle(0);
            System.out.println("\n 发邮件完成...");
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "RedisMonitorSchedule.checkRedisAndSendMail", null, null, e);
        }
    }

    @Scheduled(fixedRate = 60000L, initialDelay = 60000L)
    public void checkRedisAndSendWeixin() {
        try {
            System.out.println("\n 准备发微信...");
            professor.handle(1);
            System.out.println("\n 发微信完成...");
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "RedisMonitorSchedule.checkRedisAndSendWeixin", null, null, e);
        }
    }

}
