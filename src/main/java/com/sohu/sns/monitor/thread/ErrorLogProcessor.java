package com.sohu.sns.monitor.thread;

import com.sohu.sns.common.utils.DateUtilTool;
import com.sohu.sns.common.utils.json.JsonMapper;
import com.sohu.sns.monitor.bucket.ErrorLogBucket;
import com.sohu.sns.monitor.bucket.TimeoutBucket;
import com.sohu.sns.monitor.model.ErrorLog;
import com.sohu.sns.monitor.util.DateUtil;
import com.sohu.sns.monitor.util.ZipUtils;
import com.sohu.snscommon.dbcluster.service.MysqlClusterService;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.http.HttpClientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ErrorLog数据处理定时器
 * Created by Gary on 2015年10月20日
 */
public class ErrorLogProcessor implements Runnable {

    public static String baseUrl, stackTraceUrl, emailErrorlogInterface, smsErrorlogInterface, smsTimeoutWarnInterface;

    private static final String IS_EXISTS = "select count(1) from timeout_api_collect where appId = ? and moduleName = ?" +
            " and methodName = ? and date_str = ?";
    private static final String INSERT_DATA = "replace into timeout_api_collect set appId = ?, moduleName = ?, " +
            "methodName = ?, timeoutCount = ?, date_str = ?, updateTime = now()";
    private static final String UPDATE_DATA = "update timeout_api_collect set timeoutCount = ifnull(timeoutCount, 0)+?, " +
            "updateTime = now() where appId = ? and moduleName = ? and methodName = ? and date_str = ?";

    private int random = new Random().nextInt(100000) + 200000;
    private JsonMapper jsonMapper = JsonMapper.nonDefaultMapper();
    private MysqlClusterService mysqlClusterService;
    private boolean inProcess = false;

    public ErrorLogProcessor(MysqlClusterService mysqlClusterService, String monitorUrls) {
        Map<String, String> urls = jsonMapper.fromJson(monitorUrls, HashMap.class);
        this.baseUrl = urls.get("base_url");
        this.stackTraceUrl = urls.get("stackTrace_base_url");
        this.emailErrorlogInterface = urls.get("email_errorlog_interface");
        this.smsErrorlogInterface = urls.get("sms_errorlog_interface");
        this.smsTimeoutWarnInterface = urls.get("sms_timeout_warn_interface");
        this.mysqlClusterService = mysqlClusterService;
    }

    public void beginProcess() {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();

                if (inProcess) return;

                /**错误信息统计桶**/
                ConcurrentHashMap<String, List<ErrorLog>> bucket = ErrorLogBucket.exchange();
                /**超时统计桶*/
                ConcurrentHashMap<String, AtomicInteger> timeoutBucket = TimeoutBucket.exchange();

                try {
                    if (null != bucket && ! bucket.isEmpty()) {
                        System.out.println("error_log_send_to_server timer, bucket_size : " + bucket.size() +
                                ", time : "+ DateUtil.getCurrentTime());

                        Map<String, String> convertedMap = new HashMap<String, String>();
                        Set<String> keySet = bucket.keySet();
                        for(String key : keySet) {
                            convertedMap.put(key, jsonMapper.toJson(bucket.get(key)));
                        }
                        String errorLogs = ZipUtils.gzip(jsonMapper.toJson(convertedMap));
                        Map<String, String> errorMap = new HashMap<String, String>();
                        errorMap.put("errorLogs", errorLogs);
                        try {
                            HttpClientUtil.getStringByPost(baseUrl+emailErrorlogInterface, errorMap, null);
                        } catch (Exception e) {
                            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "send_errorLog_to_server_email", errorMap.size()+"", null, e);
                            e.printStackTrace();
                        }
                    }

                    if (null != timeoutBucket && ! timeoutBucket.isEmpty()) {
                        System.out.println("timeout_count_send_to_server timer, bucket_size : " + timeoutBucket.size() +
                                ", time : "+ DateUtil.getCurrentTime());
                        saveToDB(timeoutBucket);    //保存超时次数到数据库
                        String content = jsonMapper.toJson(timeoutBucket);
                        Map<String, String> sendMap = new HashMap<String, String>();
                        sendMap.put("timeoutCount", content);
                        try {
                            HttpClientUtil.getStringByPost(baseUrl+smsTimeoutWarnInterface, sendMap, null);
                        } catch (Exception e) {
                            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "send_timeout_warn_to_server", sendMap.size()+"", null, e);
                        } finally {
                            timeoutBucket.clear();
                        }
                    }
                } catch (Exception e) {
                    LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "ErrorLogProcessor.Timer.run", DateUtilTool.getToday(), "", e);
                } finally {
                    inProcess = false;
                    bucket.clear();
                    timeoutBucket.clear();
                    LOGGER.statLog(ModuleEnum.MONITOR_SERVICE, "ErrorLogProcessor.Timer.run", "", "", System.currentTimeMillis() - startTime, 0, 0);
                }
            }
        }, random, random);
    }

    /**
     * 保存数据到数据库
     * @param map
     */
    private void saveToDB(ConcurrentHashMap<String, AtomicInteger> map) {
        System.out.println("saveToDBTimeoutCount timer ...... time : " + DateUtil.getCurrentTime()
                + ", bucketSize:" + map.size());
        try {
            JdbcTemplate readJdbcTemplate = mysqlClusterService.getReadJdbcTemplate(null);
            JdbcTemplate writeJdbcTemplate = mysqlClusterService.getWriteJdbcTemplate(null);
            String date_str = DateUtil.getCurrentDate();
            Set<String> keySet = map.keySet();
            for (String key : keySet) {
                String[] arr = StringUtils.split(key, "_");
                if(3 != arr.length) continue;
                String appId = arr[0], moduleName = arr[1], methodName = arr[2];
                Integer timeoutCount = map.get(key).get();
                Long count = readJdbcTemplate.queryForObject(IS_EXISTS, Long.class, appId, moduleName, methodName, date_str);
                if (0 != count) {
                    writeJdbcTemplate.update(UPDATE_DATA, timeoutCount, appId, moduleName, methodName, date_str);
                } else {
                    writeJdbcTemplate.update(INSERT_DATA, appId, moduleName, methodName, timeoutCount, date_str);
                }
            }
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.SMS_EMAIL_SERVICE, "saveToDB.timeout_collect", null, null, e);
        }
    }

    @Override
    public void run() {
        beginProcess();
    }
}
