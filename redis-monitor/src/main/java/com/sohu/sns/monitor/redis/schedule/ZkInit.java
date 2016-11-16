package com.sohu.sns.monitor.redis.schedule;

import com.sohu.sns.monitor.redis.config.ZkPathConfig;
import com.sohu.sns.monitor.redis.timer.RedisDataCheckProfessor;
import com.sohu.sns.monitor.redis.util.MysqlClusterServiceUtils;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.config.ZkPathConfigure;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.zk.SnsDiamonds;
import com.sohu.snscommon.utils.zk.ZkUtils;

/**
 * Created by yzh on 2016/11/4.
 */

public class ZkInit {
    private void init(){

        try {
            System.out.println("ZkInit 初始化");
            ZkUtils zk = new ZkUtils();

            zk.connect(ZkPathConfigure.ZOOKEEPER_SERVERS, ZkPathConfigure.ZOOKEEPER_AUTH_USER,
                    ZkPathConfigure.ZOOKEEPER_AUTH_PASSWORD, ZkPathConfigure.ZOOKEEPER_TIMEOUT);

            /**监控各种urls**/
            String monitorUrls = new String(zk.getData(ZkPathConfig.MONITOR_URL_CONFIG));

            /**获取发送错误信息的配置**/
            String errorLogConfig = new String(zk.getData(ZkPathConfig.ERROR_LOG_CONFIG));

            /**获取redis检查的缓存信息**/
            String swapData = new String(zk.getData(ZkPathConfig.REDIS_CHECK_SWAP));
            RedisDataCheckProfessor.initEnv(monitorUrls, errorLogConfig, swapData, zk);

            zk.close();
            System.out.println("ZkInit 初始化完成");
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "ZkInit.init", null, null, e);
        }
    }

    public ZkInit(){
        init();
    }
}
