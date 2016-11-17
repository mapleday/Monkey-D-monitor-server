package com.sohu.sns.monitor.redis.config;

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

    public static String VISIT_ANAL_CONFIG = ZK_ROOT + "/sns_monitor/visit_analyser_info";

    public static String DUTY_CONFIG = ZK_ROOT + "/sns_monitor/duty_person_info";

    public static String REDIS_CHECK_CONFIG = ZK_ROOT + "/sns_monitor/redis_config";

    public static String ERROR_LOG_CONFIG = ZK_ROOT + "/sns_monitor/errorlog_email_config";

    public static String REDIS_CHECK_SWAP = ZK_ROOT + "/sns_monitor/redis_check_swap";

    public static String LAST_EMAIL_TIME = ZK_ROOT + "/sns_monitor/last_email_time";

    public static String LAST_EMAIL_TIME_SUB = ZK_ROOT + "/sns_monitor/last_email_time/sub";
}
