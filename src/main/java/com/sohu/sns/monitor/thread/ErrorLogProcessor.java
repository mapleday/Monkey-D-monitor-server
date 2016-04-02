package com.sohu.sns.monitor.thread;

import com.sohu.sns.common.utils.DateUtilTool;
import com.sohu.sns.common.utils.json.JsonMapper;
import com.sohu.sns.monitor.bucket.ErrorLogBucket;
import com.sohu.sns.monitor.bucket.TimeoutBucket;
import com.sohu.sns.monitor.model.ErrorLog;
import com.sohu.sns.monitor.model.MergedErrorLog;
import com.sohu.sns.monitor.util.DateUtil;
import com.sohu.sns.monitor.util.EmailStringFormatUtils;
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


    private static final String INSTANCE_COUNT = "instanceCount";
    private static final String ERROR_COUNT = "errorCount";
    private static final String ERROR_DETAIL = "errorDetail";
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
        Map<String, Object> urls = jsonMapper.fromJson(monitorUrls, HashMap.class);
        this.baseUrl = (String) urls.get("base_url");
        this.stackTraceUrl = (String) urls.get("stackTrace_base_url");
        this.emailErrorlogInterface = (String) urls.get("email_errorlog_interface");
        this.smsErrorlogInterface = (String) urls.get("sms_errorlog_interface");
        this.smsTimeoutWarnInterface = (String) urls.get("sms_timeout_warn_interface");
        this.mysqlClusterService = mysqlClusterService;
    }

    public void beginProcess() {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                try {
                    if (inProcess) return;
                    ConcurrentHashMap<String, List<ErrorLog>> bucket = ErrorLogBucket.exchange();
                    if (null != bucket && ! bucket.isEmpty()) {
                        System.out.println("error_log_send_to_server timer, bucket_size : " + bucket.size() +
                                ", time : "+ DateUtil.getCurrentTime());
                        Set<String> keySet = bucket.keySet();
                        Map<String, String> smsMap = new HashMap<String, String>();
                        Map<String, String> emailMap = new HashMap<String, String>();
                        StringBuilder emailSb = new StringBuilder();
                        smsMap.put(INSTANCE_COUNT, String.valueOf(keySet.size()));
                        emailMap.put(INSTANCE_COUNT, String.valueOf(keySet.size()));

                        int total = 0;
                        for (String instance : keySet) {
                            List<ErrorLog> errorLogs = bucket.get(instance);
                            emailSb.append(EmailStringFormatUtils.formatHead(instance));
                            Map<String, MergedErrorLog> map = new HashMap<String, MergedErrorLog>();
                            AtomicInteger errorParamsCount = new AtomicInteger(0);
                            for (ErrorLog errorLog : errorLogs) {
                                String key = errorLog.getKey();
                                if (map.containsKey(key)) {
                                    if (errorParamsCount.incrementAndGet() <= 10){
                                        map.get(key).addParams(errorLog.getParam());
                                    }
                                    map.get(key).addTimes(1);
                                } else {
                                    MergedErrorLog mergedErrorLog = new MergedErrorLog();
                                    mergedErrorLog.setErrorLog(errorLog);
                                    mergedErrorLog.addParams(errorLog.getParam());
                                    mergedErrorLog.addTimes(1);
                                    map.put(key, mergedErrorLog);
                                }
                            }

                            Set<Map.Entry<String, MergedErrorLog>> set = map.entrySet();
                            for (Map.Entry<String, MergedErrorLog> entry : set) {

                                emailSb.append(entry.getValue().getErrorLog().warpHtml())
                                        .append(EmailStringFormatUtils.formatTail(
                                                entry.getValue().getParams().toString(),
                                                stackTraceUrl + entry.getValue().getErrorLog().genParams(),
                                                entry.getValue().getTimes()));

                                total += entry.getValue().getTimes();
                            }
                            emailSb.append("</table>");
                        }
                        /**清理当前的桶**/
                        bucket.clear();

                        smsMap.put(ERROR_COUNT, String.valueOf(total));
                        emailMap.put(ERROR_DETAIL, ZipUtils.gzip(emailSb.toString()));  //发送的数据进行了压缩

                        try {
                            HttpClientUtil.getStringByGet(baseUrl+smsErrorlogInterface, smsMap);
                        } catch (Exception e) {
                            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "send_errorLog_to_server_sms", smsMap.size()+"", null, e);
                        }
                        try {
                            HttpClientUtil.getStringByPost(baseUrl+emailErrorlogInterface, emailMap, null);
                        } catch (Exception e) {
                            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "send_errorLog_to_server_email", emailMap.size()+"", null, e);
                        }

                    }

                    /**发送超时统计*/
                    ConcurrentHashMap<String, AtomicInteger> timeoutBucket = TimeoutBucket.exchange();
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
                    inProcess = false;
                } catch (Exception e) {
                    LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "ErrorLogProcessor.Timer.run", DateUtilTool.getToday(), "", e);
                } finally {
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
