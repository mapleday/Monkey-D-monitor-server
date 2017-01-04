package com.sohu.sns.monitor;

import com.sohu.sns.monitor.config.ZkPathConfig;
import com.sohu.sns.monitor.server.LogMessageProcessor;
import com.sohu.sns.monitor.thread.ErrorLogProcessor;
import com.sohu.snscommon.dbcluster.service.impl.MysqlClusterServiceImpl;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.config.ZkPathConfigure;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.spring.SpringContextUtil;
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

            /**读取kafka的配置**/
            String kafkaConfig = new String(zk.getData(ZkPathConfig.KAFKA_CONFIG));
            /**读取超时异常的种类**/
            String timeoutConfig = new String(zk.getData(ZkPathConfig.TIMEOUT_CONFIG));
            /**读取所有的kafka_topics**/
            String kafkaTopics = new String(zk.getData(ZkPathConfig.KAFKA_TOPICS_CONFIG));
            /**监控各种urls**/
            String monitorUrls = new String(zk.getData(ZkPathConfig.MONITOR_URL_CONFIG));

            new ClassPathXmlApplicationContext("classpath:com/sohu/sns/monitor/monitor-spring.xml").start();

            MysqlClusterServiceImpl mysqlClusterService = SpringContextUtil.getBean(MysqlClusterServiceImpl.class);

            /**启动监控错误日志的消费者**/
            new LogMessageProcessor(kafkaTopics, kafkaConfig, timeoutConfig).start();

            /**启动定时将日志信息发送到汇总服务器类**/
            new Thread(new ErrorLogProcessor(mysqlClusterService, monitorUrls)).start();

            LOGGER.buziLog(ModuleEnum.MONITOR_SERVICE, "SnsMonitorLogServer.start", "start ok!", "");

        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "monitor.server.start", args[0], null, e);
        }

    }
}
