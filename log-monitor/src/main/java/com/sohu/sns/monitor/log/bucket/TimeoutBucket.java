package com.sohu.sns.monitor.log.bucket;

import com.google.common.base.Strings;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 超时信息统计桶
 * Created by Gary on 2015/10/19
 */
public class TimeoutBucket {
    /**
     * 存放数据的alpha桶
     */
    private static final ConcurrentHashMap<String, AtomicInteger> bucketAlpha = new ConcurrentHashMap<String, AtomicInteger>();
    /**
     * 存放数据的beta桶
     */
    private static final ConcurrentHashMap<String,AtomicInteger> bucketBeta = new ConcurrentHashMap<String, AtomicInteger>();

    /**
     * 正在工作中的桶
     */
    private static ConcurrentHashMap<String,AtomicInteger> bucket = bucketAlpha;

    /**
     * 切换桶
     * @return
     */
    public static ConcurrentHashMap<String,AtomicInteger> exchange() {
        ConcurrentHashMap<String, AtomicInteger> lastBucket = bucket;
        if(bucket == bucketAlpha){
            bucket = bucketBeta;
        } else {
            bucket = bucketAlpha;
        }
        return lastBucket;
    }

    private static ConcurrentHashMap<String,AtomicInteger> getBucket() {
        return bucket;
    }

    /**
     * 向桶中插入数据
     * @param
     */
    public static void insertData(String key) {
        if (Strings.isNullOrEmpty(key)) {
            return;
        }
        ConcurrentHashMap<String, AtomicInteger> b = getBucket();
        AtomicInteger timeoutCount = b.get(key);
        if (null != timeoutCount) {
            timeoutCount.addAndGet(1);
        } else {
            synchronized (TimeoutBucket.class) {
                if(null != b.get(key)) {
                    insertData(key);
                } else {
                    AtomicInteger countTemp = new AtomicInteger(1);
                    b.put(key, countTemp);
                }
            }
        }
    }
}
