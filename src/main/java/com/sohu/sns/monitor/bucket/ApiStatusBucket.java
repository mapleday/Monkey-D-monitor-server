package com.sohu.sns.monitor.bucket;

import com.google.common.base.Strings;
import com.sohu.sns.monitor.model.ApiStatus;
import com.sohu.sns.monitor.model.ApiStatusCount;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Gary on 2015/11/6.
 */
public class ApiStatusBucket {
    /**
     * 存放数据的alpha桶
     */
    private static final ConcurrentHashMap<String, ApiStatusCount> bucketAlpha = new ConcurrentHashMap<String, ApiStatusCount>();
    /**
     * 存放数据的beta桶
     */
    private static final ConcurrentHashMap<String,ApiStatusCount> bucketBeta = new ConcurrentHashMap<String, ApiStatusCount>();

    /**
     * 正在工作中的桶
     */
    private static ConcurrentHashMap<String,ApiStatusCount> bucket = bucketAlpha;

    /**
     * 切换桶
     * @return
     */
    public static ConcurrentHashMap<String,ApiStatusCount> exchange() {
        ConcurrentHashMap<String, ApiStatusCount> lastBucket = bucket;
        if(bucket == bucketAlpha){
            bucket = bucketBeta;
        } else {
            bucket = bucketAlpha;
        }
        return lastBucket;
    }

    private static ConcurrentHashMap<String,ApiStatusCount> getBucket() {
        return bucket;
    }

    /**
     * 向桶中插入数据
     * @param apiStatus
     */
    public static void insertData(ApiStatus apiStatus) {
        if(null == apiStatus) {
            return;
        }
        String key = apiStatus.getMethodName();
        if (Strings.isNullOrEmpty(key)) {
            return;
        }
        ConcurrentHashMap<String, ApiStatusCount> b = getBucket();
        ApiStatusCount apiStatusCount = b.get(key);
        if (null != apiStatusCount) {
            apiStatusCount.addTimeOutCount(1);
            if(apiStatus.getCompMill() >= 1000) {
                apiStatusCount.addTimeOutCount(1);
            }
        } else {
            synchronized (ApiStatusBucket.class) {
                if(null != b.get(key)) {
                    insertData(apiStatus);
                } else {
                    ApiStatusCount apiStatusCountTemp = new ApiStatusCount(key, 1, 0);
                    if(apiStatus.getCompMill() >= 1000) {
                        apiStatusCountTemp.addTimeOutCount(1);
                    }
                    b.put(key, apiStatusCountTemp);
                }
            }
        }
    }
}
