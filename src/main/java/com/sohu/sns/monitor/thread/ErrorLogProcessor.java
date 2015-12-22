package com.sohu.sns.monitor.thread;

import com.sohu.sns.common.utils.json.JsonMapper;
import com.sohu.sns.monitor.bucket.ErrorLogBucket;
import com.sohu.sns.monitor.bucket.TimeoutBucket;
import com.sohu.sns.monitor.model.ErrorLog;
import com.sohu.sns.monitor.model.MergedErrorLog;
import com.sohu.snscommon.dbcluster.service.MysqlClusterService;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.http.HttpClientUtil;
import org.springframework.jdbc.core.JdbcTemplate;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ErrorLog数据的处理器
 * Created by Gary on 2015年10月20日
 */
public class ErrorLogProcessor implements Runnable{


    private static final String INSTANCE_COUNT = "instanceCount";
    private static final String ERROR_COUNT = "errorCount";
    private static final String ERROR_DETAIL = "errorDetail";
    private static final String BASE_URL = "http://10.10.46.44";
    private static final String QUERY_STACKTRACE_URL = "http://sns-monitor-web-test.sohusce.com/queryStackTrace";
    private static final String IS_EXISTS = "select count(1) from timeout_api_collect where appId = ? and moduleName = ? and methodName = ? and date_str = ?";
    private static final String INSERT_DATA = "replace into timeout_api_collect set appId = ?, moduleName = ?, methodName = ?, timeoutCount = ?, date_str = ?, updateTime = now()";
    private static final String UPDATE_DATA = "update timeout_api_collect set timeoutCount = ifnull(timeoutCount, 0)+?, updateTime = now() where appId = ? and moduleName = ? and methodName = ? and date_str = ?";
    private int random = new Random().nextInt(200000)+100000;
    private JsonMapper jsonMapper = JsonMapper.nonDefaultMapper();
    private MysqlClusterService mysqlClusterService;
    private boolean inProcess =  false;

    public ErrorLogProcessor(MysqlClusterService mysqlClusterService) {
        this.mysqlClusterService = mysqlClusterService;
    }
    public void beginProcess() {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(inProcess){
                    return;
                }
                ConcurrentHashMap<String, List<ErrorLog>> bucket = ErrorLogBucket.exchange();
                System.out.println("errorLogProcessor timer ... " + bucket.size());
                if(bucket != null && !bucket.isEmpty()){
                    Set<String> keySet = bucket.keySet();
                    Map<String, String> smsMap = new HashMap<String, String>();
                    Map<String, String> emailMap = new HashMap<String, String>();
                    StringBuilder emailSb = new StringBuilder();
                    smsMap.put(INSTANCE_COUNT, String.valueOf(keySet.size()));
                    emailMap.put(INSTANCE_COUNT, String.valueOf(keySet.size()));

                    int total = 0;
                    for(String instance : keySet) {
                        List<ErrorLog> errorLogs = bucket.get(instance);
                        emailSb.append("<br><div><b><font color=\"red\">"+ instance +" : </font></b></div><br>" +
                                "<table border=\"1\" cellpadding=\"0\" cellspacing=\"0\" width=\"800\" style=\"border-collapse: collapse; table-layout:fixed;\">");
                        Map<String, MergedErrorLog> map = new HashMap<String, MergedErrorLog>();

                        for(ErrorLog errorLog : errorLogs) {
                            String key = errorLog.getKey();
                            if(map.containsKey(key)) {
                                map.get(key).addParams(errorLog.getParam());
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
                        for(Map.Entry<String, MergedErrorLog> entry : set) {
                            emailSb.append(entry.getValue().getErrorLog().warpHtml() +
                                    "<tr><td align=\"center\" ><b>Params</b></td><td style=\"word-wrap:break-word;\">"+entry.getValue().getParams().toString()+"</td></tr>" +
                                    "<tr><td align=\"center\" ><b>StackTrace</b></td><td style=\"word-wrap:break-word;\"><a href=\""+QUERY_STACKTRACE_URL+entry.getValue().getErrorLog().genParams()+"\">点击查看</a></td></tr>" +
                                    "<tr><td align=\"center\"><b>出现次数</b></td><td style=\"word-wrap:break-word;\">"+entry.getValue().getTimes()+"</td></tr>" +
                                    "<tr><td colspan=\"2\">&nbsp;</td></tr>");
                            total += entry.getValue().getTimes();
                        }
                        emailSb.append("</table>");
                    }
                    //清理切换到的桶
                    bucket.clear();
                    smsMap.put(ERROR_COUNT, String.valueOf(total));
                    emailMap.put(ERROR_DETAIL, emailSb.toString());
                    String smsResult = null, emailResult = null;
                    HttpClientUtil httpClientUtil = new HttpClientUtil();
                    try {
                        smsResult = httpClientUtil.getByUtf(BASE_URL + "/sendErrorLogSms", smsMap);
                    } catch (Exception e) {
                        LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "sendErrorLogSms", smsMap.size()+"", smsResult, e);
                    }
                    try {
                        emailResult = httpClientUtil.postByUtf(BASE_URL+"/sendErrorLogEmail", emailMap, null);
                    } catch (Exception e) {
                        LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "sendErrorLogEmail", emailMap.size() + "", emailResult, e);
                    }

                }

                /**发送超时统计*/
                ConcurrentHashMap<String, AtomicLong> timeoutBucket = TimeoutBucket.exchange();
                System.out.println("timeoutCountProcessor timer ... " + timeoutBucket.size());
                if(timeoutBucket != null && !timeoutBucket.isEmpty()){
                    saveToDB(timeoutBucket);    //保存超时次数到数据库
                    String content = jsonMapper.toJson(timeoutBucket);
                    Map<String, String> sendMap = new HashMap<String, String>();
                    sendMap.put("timeoutCount", content);
                    try {
                        new HttpClientUtil().postByUtf(BASE_URL + "/sendTimeoutCount", sendMap, null);
                    } catch (Exception e) {
                        LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "sendTimeoutCount", sendMap.size()+"", null, e);
                    } finally {
                        timeoutBucket.clear();
                    }
                }
                inProcess = false;
            }
        }, random, random);
    }

    /**
     * 保存数据到数据库
     * @param map
     */
    private void saveToDB(ConcurrentHashMap<String, AtomicLong> map) {
        System.out.println("saveToDBTimeoutCount timer ...... time : " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " ,bucket:" + map.size());
        try {
            JdbcTemplate readJdbcTemplate = mysqlClusterService.getReadJdbcTemplate(null);
            JdbcTemplate writeJdbcTemplate = mysqlClusterService.getWriteJdbcTemplate(null);
            String date_str = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            Set<String> keySet = map.keySet();
            for(String key : keySet) {
                String[] arr = key.split("_");
                String appId = arr[0];
                String moduleName = arr[1];
                String methodName = arr[2];
                Long timeoutCount = map.get(key).get();
                Long count = readJdbcTemplate.queryForObject(IS_EXISTS, Long.class, appId, moduleName, methodName, date_str);
                if(0 != count) {
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
