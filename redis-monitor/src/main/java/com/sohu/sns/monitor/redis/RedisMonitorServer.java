package com.sohu.sns.monitor.redis;

import com.sohu.sns.monitor.redis.config.ZkPathConfig;
import com.sohu.sns.monitor.redis.timer.RedisDataCheckProfessor;
import com.sohu.sns.monitor.redis.util.MysqlClusterServiceUtils;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.config.ZkPathConfigure;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.zk.SnsDiamonds;
import com.sohu.snscommon.utils.zk.ZkUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * Created by yzh on 2016/11/1.
 */
public class RedisMonitorServer {

    public static void main(String[] args) {

        try {
            initZkConfig(args[0]);

            new ClassPathXmlApplicationContext("RedisMonitorContext.xml");
            MysqlClusterServiceUtils.init();
//            new RedisDataCheckProfessor().handle();
//            System.in.read();
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "RedisMonitorServer", null, null, e);
            e.printStackTrace();
        }
    }

    public static void  initZkConfig(String arg) {
        try {
            ZkUtils.setZkConfigFilePath(arg);
            ZkUtils.initZkConfig(arg);

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
