package com.sohu.sns.monitor.server;

import com.codahale.metrics.MetricRegistry;
import com.sohu.sns.monitor.server.consumer.MonitorErrorLogConsumer;
import com.sohu.snscommon.dbcluster.service.impl.MysqlClusterServiceImpl;
import com.sohu.snscommon.kafka.Kafka;
import com.sohu.snscommon.utils.config.ZkPathConfigure;
import com.sohu.snscommon.utils.spring.SpringContextUtil;
import com.sohu.snscommon.utils.zk.ZkUtils;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by morgan on 15/9/22.
 */
public class LogMessageProcessor {

    private static final String topicName = "monitor_log_topic";
    private static final String groupName = "monitor_log_group01";


    public void initConsumer() throws IOException, InterruptedException, KeeperException {
        ZkUtils zk = new ZkUtils();
        zk.connect(ZkPathConfigure.ZOOKEEPER_SERVERS, ZkPathConfigure.ZOOKEEPER_AUTH_USER,
                ZkPathConfigure.ZOOKEEPER_AUTH_PASSWORD, ZkPathConfigure.ZOOKEEPER_TIMEOUT);
        String kafkaConfig = new String(zk.getData(ZkPathConfigure.ROOT_NODE + "/sns_kafka"));
        Kafka kafka = new Kafka(kafkaConfig, new MetricRegistry());
        /**超时的异常种类*/
        List<String> exceptionList = Arrays.asList("java.net.SocketTimeoutException", "org.apache.commons.httpclient.ConnectTimeoutException");
        kafka.consumeForever(topicName, groupName, 1, new MonitorErrorLogConsumer(SpringContextUtil.getBean(MysqlClusterServiceImpl.class), exceptionList));
    }

    public void start() {

        try {
            initConsumer();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

}
