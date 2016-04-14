package com.sohu.sns.monitor;

import com.sohu.sns.monitor.config.MySqlDBConfig;
import com.sohu.sns.monitor.server.LogMessageProcessor;
import com.sohu.sns.monitor.server.MessageProcessor;
import com.sohu.sns.monitor.server.config.UniqNameDBClusterService;
import com.sohu.sns.monitor.service.ServiceImpl.SelectPersonServiceImpl;
import com.sohu.sns.monitor.thread.ErrorLogProcessor;
import com.sohu.sns.monitor.timer.StatLogVisitAnalyzer;
import com.sohu.sns.monitor.util.HttpApiServer;
import com.sohu.snscommon.dbcluster.service.impl.MysqlClusterServiceImpl;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.config.ZkPathConfigure;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.spring.SpringContextUtil;
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


            /**读取kafka的配置**/
            String kafkaConfig = new String(zk.getData(ZkPathConfigure.ROOT_NODE + "/sns_kafka_super"));
            /**读取超时异常的种类**/
            String timeoutConfig = new String(zk.getData(ZkPathConfigure.ROOT_NODE + "/sns_monitor/timeout_config"));
            /**读取所有的kafka_topics**/
            String kafkaTopics = new String(zk.getData(ZkPathConfigure.ROOT_NODE + "/sns_kafka_topics"));
            /**监控各种urls**/
            String monitorUrls = new String(zk.getData(ZkPathConfigure.ROOT_NODE + "/sns_monitor/monitor_urls"));
            /**获取异常访问分析的相关配置**/
            String visitAnalyserInfo = new String(zk.getData(ZkPathConfigure.ROOT_NODE + "/sns_monitor/visit_analyser_info"));
            /**获取值班配置信息**/
            String dutyConfigInfo = new String(zk.getData(ZkPathConfigure.ROOT_NODE + "/sns_monitor/duty_person_info"));

            /**初始化异常访问分析所需要的环境**/
            StatLogVisitAnalyzer.initEnv(monitorUrls, visitAnalyserInfo);
            /**初始化值班相关配置**/
            SelectPersonServiceImpl.initEnv(monitorUrls, dutyConfigInfo);

            new ClassPathXmlApplicationContext("classpath:monitor/monitor-spring.xml");

            MysqlClusterServiceImpl bean = SpringContextUtil.getBean(MysqlClusterServiceImpl.class);
            bean.init(new MySqlDBConfig());

            /** 唯一名线上数据库数据源**/
            UniqNameDBClusterService uniqNameBean = SpringContextUtil.getBean(UniqNameDBClusterService.class);
            uniqNameBean.init(null);

            /**启动监控错误日志的消费者**/
            new LogMessageProcessor(kafkaTopics, kafkaConfig, timeoutConfig).start();

            /**启动url_log的监控**/
            //new MessageProcessor().start();

            /**启动定时将日志信息发送到汇总服务器类**/
            new Thread(new ErrorLogProcessor(bean, monitorUrls)).start();

        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "monitor.server.start", args[0], null, e);
            e.printStackTrace();
        }

        try {
            new HttpApiServer(Integer.parseInt(args[1])).run();
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "SnsMonitorLogServer.main", null, "SnsMonitorLogServer api start up failure !", e);
        }
    }
}
