package com.sohu.sns.monitor.log.thread;

import com.sohu.sns.common.utils.DateUtilTool;
import com.sohu.sns.common.utils.json.JsonMapper;
import com.sohu.sns.monitor.log.bucket.TimeoutBucket;
import com.sohu.sns.monitor.common.dao.timeoutApiCollect.TimeoutApiCollectDao;
import com.sohu.sns.monitor.common.module.TimeoutApiCollect;
import com.sohu.sns.monitor.log.config.ZkPathConfig;
import com.sohu.sns.monitor.log.util.DateUtil;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.http.HttpClientUtil;
import com.sohu.snscommon.utils.zk.SnsDiamonds;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ErrorLog数据处理定时器
 * Created by Gary on 2015年10月20日
 */
@Component
public class ErrorLogProcessor {
    private static String baseUrl;
    private static String smsTimeoutWarnInterface;
    private static JsonMapper jsonMapper = JsonMapper.nonDefaultMapper();
    private boolean inProcess = false;

    @Autowired
    TimeoutApiCollectDao timeoutApiCollectDao;


    @PostConstruct
    public void init() {
        String monitorUrls = SnsDiamonds.getZkData(ZkPathConfig.MONITOR_URL_CONFIG);
        Map<String, String> urls = jsonMapper.fromJson(monitorUrls, HashMap.class);
        ErrorLogProcessor.baseUrl = urls.get("base_url");
        ErrorLogProcessor.smsTimeoutWarnInterface = urls.get("sms_timeout_warn_interface");
    }

    @Scheduled(fixedDelay = 1000)
    public void run() {
        long startTime = System.currentTimeMillis();

        if (inProcess) return;

        /**超时统计桶*/
        ConcurrentHashMap<String, AtomicInteger> timeoutBucket = TimeoutBucket.exchange();

        try {
            if (null != timeoutBucket && !timeoutBucket.isEmpty()) {
                System.out.println("timeout_count_send_to_server timer, bucket_size : " + timeoutBucket.size() +
                        ", time : " + DateUtil.getCurrentTime());
                saveToDB(timeoutBucket);    //保存超时次数到数据库
                String content = jsonMapper.toJson(timeoutBucket);
                Map<String, String> sendMap = new HashMap<String, String>();
                sendMap.put("timeoutCount", content);
                try {
                    HttpClientUtil.getStringByPost(baseUrl + smsTimeoutWarnInterface, sendMap, null);
                } catch (Exception e) {
                    LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "send_timeout_warn_to_server", sendMap.size() + "", null, e);
                } finally {
                    timeoutBucket.clear();
                }
            }
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "ErrorLogProcessor.Timer.run", DateUtilTool.getToday(), "", e);
        } finally {
            inProcess = false;
            timeoutBucket.clear();
            LOGGER.statLog(ModuleEnum.MONITOR_SERVICE, "ErrorLogProcessor.Timer.run", "", "", System.currentTimeMillis() - startTime, 0, 0);
        }
    }


    /**
     * 保存数据到数据库
     *
     * @param map
     */
    private void saveToDB(ConcurrentHashMap<String, AtomicInteger> map) {
        try {
            String dateStr = DateUtil.getCurrentDate();
            Set<String> keySet = map.keySet();
            for (String key : keySet) {
                String[] arr = StringUtils.split(key, "_");
                if (3 != arr.length) continue;
                String appId = arr[0], moduleName = arr[1], methodName = arr[2];
                Integer timeoutCount = map.get(key).get();

                TimeoutApiCollect collect = new TimeoutApiCollect();
                collect.setAppId(appId);
                collect.setDateStr(dateStr);
                collect.setMethodName(methodName);
                collect.setModuleName(moduleName);
                collect.setUpdateTime(new Date());
                collect.setTimeoutCount(timeoutCount);

                int count = timeoutApiCollectDao.getTimeOutCount(collect);
                if (0 != count) {
                    timeoutApiCollectDao.updateTimeOutCount(collect);
                } else {
                    timeoutApiCollectDao.saveTimeOut(collect);
                }
            }
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.SMS_EMAIL_SERVICE, "saveToDB.timeout_collect", null, null, e);
        }
    }

}
