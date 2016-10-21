package com.sohu.sns.monitor.server;

import com.codahale.metrics.MetricRegistry;
import com.sohu.sns.monitor.server.consumer.MonitorApiStatusConsumer;
import com.sohu.snscommon.kafka.Kafka;
import com.sohu.snscommon.utils.config.ZkPathConfigure;
import com.sohu.snscommon.utils.zk.ZkUtils;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;

/**
 * Created by Gary on 2015/11/6.
 */
public class ApiStatusProfessor {

    private static final String topicName = "monitor_api_status_topic";
    private static final String groupName = "monitor_api_status_group01";


    public void initConsumer() throws IOException, InterruptedException, KeeperException {
        ZkUtils zk = new ZkUtils();
        zk.connect(ZkPathConfigure.ZOOKEEPER_SERVERS, ZkPathConfigure.ZOOKEEPER_AUTH_USER,
                ZkPathConfigure.ZOOKEEPER_AUTH_PASSWORD, ZkPathConfigure.ZOOKEEPER_TIMEOUT);
        String kafkaConfig = new String(zk.getData(ZkPathConfigure.ROOT_NODE + "/sns_kafka"));
        Kafka kafka = new Kafka(kafkaConfig, new MetricRegistry());
        kafka.consumeForever(topicName, groupName, 1, new MonitorApiStatusConsumer());
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
