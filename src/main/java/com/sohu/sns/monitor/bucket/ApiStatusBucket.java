package com.sohu.sns.monitor.bucket;

import com.google.common.base.Strings;
import com.sohu.sns.monitor.model.ApiStatus;
import com.sohu.sns.monitor.util.DateUtil;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Gary on 2015/11/6.
 */
public class ApiStatusBucket {
    /**
     * 存放数据的alpha桶
     */
    private static final ConcurrentHashMap<String, ApiStatus> bucketAlpha = new ConcurrentHashMap<String, ApiStatus>();
    /**
     * 存放数据的beta桶
     */
    private static final ConcurrentHashMap<String,ApiStatus> bucketBeta = new ConcurrentHashMap<String, ApiStatus>();

    /**
     * 正在工作中的桶
     */
    private static ConcurrentHashMap<String,ApiStatus> bucket = bucketAlpha;

    /**
     * 切换桶
     * @return
     */
    public static ConcurrentHashMap<String,ApiStatus> exchange() {
        ConcurrentHashMap<String, ApiStatus> lastBucket = bucket;
        if(bucket == bucketAlpha){
            bucket = bucketBeta;
        } else {
            bucket = bucketAlpha;
        }
        return lastBucket;
    }

    private static ConcurrentHashMap<String,ApiStatus> getBucket() {
        return bucket;
    }

    /**
     * 向桶中插入数据
     * @param
     */
    public static void insertData(String moduleName, String method, boolean timeOut) {
        if (Strings.isNullOrEmpty(method) || Strings.isNullOrEmpty(moduleName)) {
            return;
        }
        ConcurrentHashMap<String, ApiStatus> b = getBucket();
        String key = moduleName + "_" + method;
        ApiStatus apiStatus = b.get(key);
        if (null != apiStatus) {
            apiStatus.addUseCount(1);
            if(timeOut) {
                apiStatus.addTimeOutCount(1);
            }
        } else {
            synchronized (ApiStatusBucket.class) {
                if(null != b.get(key)) {
                    insertData(moduleName, method, timeOut);
                } else {
                    ApiStatus apiStatusTemp = new ApiStatus(moduleName, method, 1, 0);
                    if(timeOut) {
                        apiStatusTemp.addTimeOutCount(1);
                    }
                    b.put(key, apiStatusTemp);
                }
            }
        }
        LOGGER.buziLog(ModuleEnum.MONITOR_SERVICE, "bucket.insertData."+moduleName, method+"_"+timeOut, "bucketSize:"+b.size()+","+ DateUtil.getCurrentDate());
    }
}
