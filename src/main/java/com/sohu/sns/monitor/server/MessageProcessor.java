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
import com.sohu.sns.monitor.server.dao.MonitorUrlDAO;
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
public class MessageProcessor {

    //message 结构
    private static final Schema<TraceLog> schema;

    private static final String topicName = "monitor_topic";
    private static final String groupName = "monitor_group01";

    static {
        ExplicitIdStrategy.Registry r = new ExplicitIdStrategy.Registry();
        r = r.registerPojo(TraceLog.class, 1);
        r = r.registerPojo(UrlTraceLog.class, 2);
        r = r.registerPojo(ExceptionInfo.class, 3);
        r = r.registerPojo(MethodTraceLog.class, 4);
        schema = RuntimeSchema.getSchema(TraceLog.class, r.strategy);
    }

    public static Schema<TraceLog> getSchema() {
        return schema;
    }

    public void initConsumer() throws IOException, InterruptedException, KeeperException {
        ZkUtils zk = new ZkUtils();
        zk.connect(ZkPathConfigure.ZOOKEEPER_SERVERS, ZkPathConfigure.ZOOKEEPER_AUTH_USER,
                ZkPathConfigure.ZOOKEEPER_AUTH_PASSWORD, ZkPathConfigure.ZOOKEEPER_TIMEOUT);
        String kafkaConfig = new String(zk.getData(ZkPathConfigure.ROOT_NODE + "/sns_kafka"));
        Kafka kafka = new Kafka(kafkaConfig, new MetricRegistry());
        kafka.consumeForever(topicName, groupName, 1, new MonitorConsumer(SpringContextUtil.getBean(MonitorUrlHBaseDAO.class)));
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
