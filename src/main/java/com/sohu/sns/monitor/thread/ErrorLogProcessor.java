package com.sohu.sns.monitor.thread;

import com.sohu.sns.monitor.bucket.ErrorLogBucket;
import com.sohu.sns.monitor.model.ErrorLog;
import com.sohu.sns.monitor.model.MergedErrorLog;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.http.HttpClientUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ErrorLog数据的处理器
 * Created by Gary on 2015年10月20日
 */
public class ErrorLogProcessor implements Runnable{


    private static final String INSTANCE_COUNT = "instanceCount";
    private static final String ERROR_COUNT = "errorCount";
    private static final String ERROR_DETAIL = "errorDetail";
    private static final String BASE_URL = "http://10.10.46.44";

    private boolean inProcess =  false;

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
                        emailSb.append("<META http-equiv=Content-Type content='text/html; charset=GBK'><br><br>&nbsp;&nbsp;<b>" + instance +"</b> : <br>" +
                                "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<table border=\"1\" cellspacing=\"0\">");
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
                                    "<tr><td><b><font color='red'>Params</font></b></td><td>"+entry.getValue().getParams().toString()+"</td></tr>" +
                                    "<tr><td><b><font color='red'>出现次数</font></b></td><td>"+entry.getValue().getTimes()+"</td></tr>" +
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

                inProcess = false;

            }
        }, 300000, 300000);
    }

    @Override
    public void run() {
        beginProcess();
    }
}
