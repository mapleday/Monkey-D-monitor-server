package com.sohu.sns.monitor.config;

import com.sohu.snscommon.utils.config.ZkPathConfigure;

/**
 * Created by Gary Chan on 2016/4/15.
 */
public class ZkPathConfig {

    private static String ZK_ROOT = ZkPathConfigure.ROOT_NODE;

    public static String KAFKA_CONFIG = ZK_ROOT + "/sns_kafka_super";

    public static String TIMEOUT_CONFIG = ZK_ROOT + "/sns_monitor/timeout_config";

    public static String KAFKA_TOPICS_CONFIG = ZK_ROOT + "/sns_kafka_topics";

    public static String MONITOR_URL_CONFIG = ZK_ROOT + "/sns_monitor/monitor_urls";
}
