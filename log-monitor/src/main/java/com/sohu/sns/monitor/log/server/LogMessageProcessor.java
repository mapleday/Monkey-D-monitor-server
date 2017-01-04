package com.sohu.sns.monitor.log.server;

import com.codahale.metrics.MetricRegistry;
import com.sohu.sns.common.utils.json.JsonMapper;
import com.sohu.sns.monitor.log.config.ZkPathConfig;
import com.sohu.sns.monitor.log.server.consumer.MonitorErrorLogConsumer;
import com.sohu.snscommon.kafka.Kafka;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.zk.SnsDiamonds;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.KeeperException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by morgan on 15/9/22.
 * 错误日志收集
 */
@Component
public class LogMessageProcessor {
    private String topicName;   //kafka_topic
    private String groupName;   //kafka_group
    private String kafkaConfig; //kafka配置
    private String timeoutTypesStr;  //超时异常的种类
    private String monitorMethods;    //需要发送监控短信的接口
    private JsonMapper jsonMapper = JsonMapper.nonDefaultMapper();

    /**
     * '
     * 根据kafka的配置和topic & group等信息初始化消费者
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws KeeperException
     */
    private void initConsumer() throws IOException, InterruptedException, KeeperException {
        /**初始化kafka**/
        Kafka kafka = new Kafka(kafkaConfig, new MetricRegistry());

        /**解析超时异常的种类**/
        Set<String> timeoutTypes = new HashSet<String>();
        if (null != timeoutTypesStr) {
            String[] timeoutArray = StringUtils.split(timeoutTypesStr, ",");
            for (String str : timeoutArray) {
                timeoutTypes.add(str.trim());
            }
        }

        /**解析需要发送监控短信的接口**/
        Set<String> methodsTypes = new HashSet<String>();
        if (null != monitorMethods) {
            String[] methodsArray = StringUtils.split(monitorMethods, ",");
            for (String str : methodsArray) {
                methodsTypes.add(str);
            }
        }

        kafka.consumeForever(topicName, groupName, 1, new MonitorErrorLogConsumer());
    }

    @PostConstruct
    public void start() {
        try {
            /**读取kafka的配置**/
            String kafkaConfig = SnsDiamonds.getZkData(ZkPathConfig.KAFKA_CONFIG);
            /**读取超时异常的种类**/
            String timeoutConfig = SnsDiamonds.getZkData((ZkPathConfig.TIMEOUT_CONFIG));
            /**读取所有的kafka_topics**/
            String kafkaTopics = SnsDiamonds.getZkData((ZkPathConfig.KAFKA_TOPICS_CONFIG));

            Map<String, Object> kafkaTopicsMap = jsonMapper.fromJson(kafkaTopics, HashMap.class);
            Map<String, Object> timeoutConfigMap = jsonMapper.fromJson(timeoutConfig, HashMap.class);
            this.topicName = (String) kafkaTopicsMap.get("monitor_log_topic");
            this.groupName = (String) kafkaTopicsMap.get("monitor_log_group");
            this.kafkaConfig = kafkaConfig;
            this.timeoutTypesStr = (String) timeoutConfigMap.get("timeout_exceptions");
            this.monitorMethods = (String) timeoutConfigMap.get("monitor_methods");

            initConsumer();
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "LogMessageProcessor.start", "", "", e);
        }
    }

}
