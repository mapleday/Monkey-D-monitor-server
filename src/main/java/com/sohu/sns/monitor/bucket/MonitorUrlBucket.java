package com.sohu.sns.monitor.bucket;

import com.sohu.sns.monitor.model.MonitorUrl;
import com.sohu.sns.monitor.model.UrlInstanceInfo;
import com.sohu.sns.monitor.util.DateUtil;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Gary Chan on 2016/4/12.
 */
public class MonitorUrlBucket {

    private static Object lock = new Object();
    /**
     * 存放数据的alpha桶
     */
    private static final Map<String, Map<String, UrlInstanceInfo>> bucketAlpha
            = new ConcurrentHashMap<String, Map<String, UrlInstanceInfo>>();
    /**
     * 存放数据的beta桶
     */
    private static final Map<String, Map<String, UrlInstanceInfo>> bucketBeta
            = new ConcurrentHashMap<String, Map<String, UrlInstanceInfo>>();

    /**
     * 正在工作中的桶
     */
    private static Map<String,Map<String, UrlInstanceInfo>> bucket = bucketAlpha;

    /**
     * 切换桶
     * @return
     */
    public static Map<String, Map<String, UrlInstanceInfo>> exchange() {
        Map<String, Map<String, UrlInstanceInfo>> lastBucket = bucket;
        if(bucket == bucketAlpha){
            bucket = bucketBeta;
        } else {
            bucket = bucketAlpha;
        }
        return lastBucket;
    }

    private static Map<String,Map<String, UrlInstanceInfo>> getBucket() {
        return bucket;
    }

    /**
     * 向桶中插入数据
     * @param
     */
    public static void insertData(MonitorUrl monitorUrl) {
        if (null == monitorUrl) {
            return;
        }
        Map<String, Map<String, UrlInstanceInfo>> b = getBucket();
        String url = monitorUrl.getUrl();
        String appIns = monitorUrl.getAppId();
        Map<String, UrlInstanceInfo> map = b.get(url);
        if (null != map) {
            insertUrlInstanceInfo(monitorUrl, map);
        } else {
            synchronized (MonitorUrlBucket.class) {
                if(null != b.get(url)) {
                    insertData(monitorUrl);
                } else {
                    Map<String, UrlInstanceInfo> tempMap = new ConcurrentHashMap<String, UrlInstanceInfo>();
                    UrlInstanceInfo urlInstanceInfo = new UrlInstanceInfo();
                    urlInstanceInfo.addConsumeTime(monitorUrl.getConsumeTime());
                    urlInstanceInfo.addExceptionNum(monitorUrl.getHasException());
                    tempMap.put(appIns, urlInstanceInfo);
                    b.put(url, tempMap);
                }
            }
        }
        LOGGER.buziLog(ModuleEnum.MONITOR_SERVICE, "MonitorUrlBucket.insertData", null, "bucketSize:" + b.size() + "," + DateUtil.getCurrentDate());
    }

    private static void insertUrlInstanceInfo(MonitorUrl monitorUrl, Map<String, UrlInstanceInfo> map) {
        String appIns = monitorUrl.getAppId();
        UrlInstanceInfo urlInstanceInfo = map.get(appIns);
        if(null != urlInstanceInfo) {
            urlInstanceInfo.addConsumeTime(monitorUrl.getConsumeTime());
            urlInstanceInfo.addExceptionNum(monitorUrl.getHasException());
        } else {
            synchronized (lock) {
                if(null != map.get(appIns)) {
                    insertUrlInstanceInfo(monitorUrl, map);
                } else {
                    UrlInstanceInfo temp = new UrlInstanceInfo();
                    temp.addConsumeTime(monitorUrl.getConsumeTime());
                    temp.addExceptionNum(monitorUrl.getHasException());
                    map.put(appIns, temp);
                }
            }
        }
    }
}
