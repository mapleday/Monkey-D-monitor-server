package com.sohu.sns.monitor.bucket;

import com.google.common.base.Strings;
import com.sohu.sns.monitor.model.ErrorLog;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 接收ErrorLog数据的双缓冲
 * Created by Gary on 2015/10/19
 */
public class ErrorLogBucket {
    /**
     * 存放数据的alpha桶
     */
    private static final ConcurrentHashMap<String, List<ErrorLog>> bucketAlpha = new ConcurrentHashMap<String, List<ErrorLog>>();
    /**
     * 存放数据的beta桶
     */
    private static final ConcurrentHashMap<String,List<ErrorLog>> bucketBeta = new ConcurrentHashMap<String, List<ErrorLog>>();

    /**
     * 正在工作中的桶
     */
    private static ConcurrentHashMap<String,List<ErrorLog>> bucket = bucketAlpha;

    /**
     * 切换桶
     * @return
     */
    public static ConcurrentHashMap<String,List<ErrorLog>> exchange() {
        ConcurrentHashMap<String, List<ErrorLog>> lastBucket = bucket;
        if(bucket == bucketAlpha){
            bucket = bucketBeta;
        } else {
            bucket = bucketAlpha;
        }
        return lastBucket;
    }

    private static ConcurrentHashMap<String,List<ErrorLog>> getBucket() {
        return bucket;
    }

    /**
     * 向桶中插入数据
     * @param errorLog 错误日志
     */
    public static void insertData(ErrorLog errorLog) {
        if(null == errorLog) {
            return;
        }
        String key = errorLog.getAppId() + "_" + errorLog.getInstanceId();
        if (Strings.isNullOrEmpty(key)) {
            return;
        }
        ConcurrentHashMap<String, List<ErrorLog>> b = getBucket();
        List<ErrorLog> errorLogs = b.get(key);
        if (null != errorLogs) {
            errorLogs.add(errorLog);
        } else {
            synchronized (ErrorLogBucket.class) {
                if(null != b.get(key)) {
                    insertData(errorLog);
                } else {
                    List<ErrorLog> list = new LinkedList<ErrorLog>();
                    list.add(errorLog);
                    b.put(key, list);
                }
            }
        }
    }
}
