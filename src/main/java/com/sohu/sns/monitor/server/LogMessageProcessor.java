package com.sohu.sns.monitor.server;

import com.codahale.metrics.MetricRegistry;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.ExplicitIdStrategy;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.sohu.sns.monitor.agent.store.model.exception.ExceptionInfo;
import com.sohu.sns.monitor.agent.store.model.url.MethodTraceLog;
import com.sohu.sns.monitor.agent.store.model.url.TraceLog;
import com.sohu.sns.monitor.agent.store.model.url.UrlTraceLog;
import com.sohu.sns.monitor.server.consumer.MonitorConsumer;
import com.sohu.sns.monitor.server.dao.MonitorUrlHBaseDAO;
import com.sohu.snscommon.kafka.Kafka;
import com.sohu.snscommon.utils.config.ZkPathConfigure;
import com.sohu.snscommon.utils.spring.SpringContextUtil;
import com.sohu.snscommon.utils.zk.ZkUtils;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;

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
        //todo 换成新的消费者
//        kafka.consumeForever(topicName, groupName, 1, new MonitorConsumer(SpringContextUtil.getBean(MonitorUrlHBaseDAO.class)));
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
