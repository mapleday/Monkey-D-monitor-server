package com.sohu.sns.monitor.log;

import com.sohu.sns.monitor.log.config.ZkPathConfig;
import com.sohu.sns.monitor.log.server.LogMessageProcessor;
import com.sohu.sns.monitor.log.thread.ErrorLogProcessor;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.config.ZkPathConfigure;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.zk.SnsDiamonds;
import com.sohu.snscommon.utils.zk.ZkUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by morgan on 15/9/22.
 */
public class SnsMonitorLogServer {

    public static void main(String[] args) {

        try {

            ZkUtils.setZkConfigFilePath(args[0]);
            ZkUtils.initZkConfig(args[0]);

            ZkUtils zk = new ZkUtils();
            zk.connect(ZkPathConfigure.ZOOKEEPER_SERVERS, ZkPathConfigure.ZOOKEEPER_AUTH_USER,
                    ZkPathConfigure.ZOOKEEPER_AUTH_PASSWORD, ZkPathConfigure.ZOOKEEPER_TIMEOUT);
            SnsDiamonds.setDiamondsEnvBySystem();

            new ClassPathXmlApplicationContext("classpath:logSpringConetxt.xml").start();

            LOGGER.buziLog(ModuleEnum.MONITOR_SERVICE, "SnsMonitorLogServer.start", "start ok!", "");

        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "monitor.server.start", args[0], null, e);
        }

    }
}
