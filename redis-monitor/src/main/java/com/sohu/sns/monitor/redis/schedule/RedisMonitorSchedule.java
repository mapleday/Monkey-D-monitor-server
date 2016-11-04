package com.sohu.sns.monitor.redis.schedule;

import com.sohu.sns.monitor.redis.config.ZkPathConfig;
import com.sohu.sns.monitor.redis.timer.RedisDataCheckProfessor;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.config.ZkPathConfigure;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.exception.SnsConfigException;
import com.sohu.snscommon.utils.zk.SnsDiamonds;
import com.sohu.snscommon.utils.zk.ZkUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by yzh on 2016/11/3.
 */
@Component
public class RedisMonitorSchedule {
    private static boolean  isInited = false;


    @Scheduled(fixedRate = 10000l)
    public void checkRedis(){
        try {
//            if(!isInited) {
//                init();
//                isInited=true;
//            }
            new RedisDataCheckProfessor().handle();
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "RedisMonitorServer", null, null, e);
            e.printStackTrace();
        }
    }
}
