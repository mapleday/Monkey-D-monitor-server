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
    private static String param = "F:\\IdeaProjects\\sns-monitor-server\\redis-monitor\\src\\env\\test\\zk\\zk.json";
    private static boolean  isInited = false;
    private void init(){
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++");
        try {
            ZkUtils.setZkConfigFilePath(param);
            ZkUtils.initZkConfig(param);
            ZkUtils zk = new ZkUtils();

            zk.connect(ZkPathConfigure.ZOOKEEPER_SERVERS, ZkPathConfigure.ZOOKEEPER_AUTH_USER,
                    ZkPathConfigure.ZOOKEEPER_AUTH_PASSWORD, ZkPathConfigure.ZOOKEEPER_TIMEOUT);
            SnsDiamonds.setDiamondsEnvBySystem();

            /**监控各种urls**/
            String monitorUrls = new String(zk.getData(ZkPathConfig.MONITOR_URL_CONFIG));

            /**获取发送错误信息的配置**/
            String errorLogConfig = new String(zk.getData(ZkPathConfig.ERROR_LOG_CONFIG));

            /**获取redis检查的缓存信息**/
            String swapData = new String(zk.getData(ZkPathConfig.REDIS_CHECK_SWAP));
            RedisDataCheckProfessor.initEnv(monitorUrls, errorLogConfig, swapData, zk);
            zk.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Scheduled(fixedRate = 10000l)
    public void checkRedis(){
        try {
            if(!isInited) {
                init();
                isInited=true;
            }
            new RedisDataCheckProfessor().handle();
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "RedisMonitorServer", null, null, e);
            e.printStackTrace();
        }
    }
}
