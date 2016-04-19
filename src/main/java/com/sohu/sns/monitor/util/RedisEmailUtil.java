package com.sohu.sns.monitor.util;

/**
 * Created by Gary Chan on 2016/4/16.
 */
public class RedisEmailUtil {

    public static final String SUBJECT = "Redis异常预测";
    public static final String TIME = "\n\r当前检查时间 : %s, 上次检查时间 : %s";
    public static final String VISIT_EXCEPTION = "\n\r1.未能成功访问的Redis实例或uid为: %s";
    public static final String KEYS_EXCEPTION = "\n\r2.keys不一致的Master-Slave结点为: %s";
    public static final String GROW_EXCEPTION = "\n\r3.KEYS较上次统计增幅超过10%%的Redis实例为: %s";
    public static final String DECLINE_EXCEPTION = "\n\r4.KEYS较上次统计下降10%%的Redis实例为: %s";
}
