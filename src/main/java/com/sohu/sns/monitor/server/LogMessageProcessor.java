package com.sohu.sns.monitor.server;

import com.codahale.metrics.MetricRegistry;
import com.sohu.sns.common.utils.json.JsonMapper;
import com.sohu.sns.monitor.server.consumer.MonitorErrorLogConsumer;
import com.sohu.snscommon.dbcluster.service.ds.ConsistHashCircle;
import com.sohu.snscommon.dbcluster.service.impl.MysqlClusterServiceImpl;
import com.sohu.snscommon.kafka.Kafka;
import com.sohu.snscommon.utils.config.ZkPathConfigure;
import com.sohu.snscommon.utils.spring.SpringContextUtil;
import com.sohu.snscommon.utils.zk.ZkUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hbase.util.Hash;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.*;

/**
 * Created by morgan on 15/9/22.
 */
public class LogMessageProcessor {

    private String topicName;   //kafka_topic
    private String groupName;   //kafka_group
    private String kafkaConfig; //kafka配置
    private String timeoutTypesStr;  //超时异常的种类

    private JsonMapper jsonMapper = JsonMapper.nonDefaultMapper();

    public LogMessageProcessor(String kafkaTopics, String kafkaConfig, String timeoutConfig) {
        Map<String, Object> kafkaTopicsMap = jsonMapper.fromJson(kafkaTopics, HashMap.class);
        Map<String, Object> timeoutConfigMap = jsonMapper.fromJson(timeoutConfig, HashMap.class);
        this.topicName = (String) kafkaTopicsMap.get("monitor_log_topic");
        this.groupName = (String) kafkaTopicsMap.get("monitor_log_group");
        this.kafkaConfig = kafkaConfig;
        this.timeoutTypesStr = (String) timeoutConfigMap.get("timeout_exceptions");
    }

    /**'
     * 根据kafka的配置和topic & group等信息初始化消费者
     * @throws IOException
     * @throws InterruptedException
     * @throws KeeperException
     */
    public void initConsumer() throws IOException, InterruptedException, KeeperException {
        /**初始化kafka**/
        Kafka kafka = new Kafka(kafkaConfig, new MetricRegistry());

        /**解析超时异常的种类**/
        Set<String> timeoutTypes = new HashSet<String>();
        if(null != timeoutTypesStr) {
            String[] timeoutArray = StringUtils.split(timeoutTypesStr, ",");
            for(String str : timeoutArray) {
                timeoutTypes.add(str.trim());
            }
        }
        kafka.consumeForever(topicName, groupName, 1, new MonitorErrorLogConsumer(SpringContextUtil.getBean(MysqlClusterServiceImpl.class), timeoutTypes));
    }

    public void start() {
        try {
            initConsumer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
