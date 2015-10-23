package com.sohu.sns.monitor.server.consumer;

import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.google.common.base.Function;
import com.sohu.sns.monitor.agent.store.model.url.MethodTraceLog;
import com.sohu.sns.monitor.agent.store.model.url.TraceLog;
import com.sohu.sns.monitor.agent.store.model.url.UrlTraceLog;
import com.sohu.sns.monitor.model.MethodLog;
import com.sohu.sns.monitor.model.MonitorUrl;
import com.sohu.sns.monitor.server.MessageProcessor;
import com.sohu.sns.monitor.server.dao.MonitorUrlHbaseDAO;
import com.sohu.sns.monitor.util.ProtobufUtil;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by morgan on 15/9/22.
 */
public class MonitorConsumer implements Function<byte[], Boolean> {
    private MonitorUrlHbaseDAO monitorUrlHBaseDAO;


    public MonitorConsumer(MonitorUrlHbaseDAO monitorUrlHBaseDAO) {
        this.monitorUrlHBaseDAO = monitorUrlHBaseDAO;
    }
    @Override
    public Boolean apply(byte[] input) {
        byte[] data = ProtobufUtil.decompress(input);
        TraceLog traceLog = new TraceLog();
        ProtostuffIOUtil.mergeFrom(data, traceLog, MessageProcessor.getSchema());
        for (UrlTraceLog urlTraceLog : traceLog.getUrlTraceLogs()) {
            System.out.println(urlTraceLog.getApplicationId()
            + "time use="+urlTraceLog.getConsumeTime()+" \t\ttime use="+":url="+urlTraceLog.getUrl()
            + "\thas error="+urlTraceLog.getHasException());
            MonitorUrl monitorUrl = new MonitorUrl();
            String appIns = urlTraceLog.getApplicationId();
            String[] split = appIns.split("_");
            if (split.length > 1) {
                monitorUrl.setAppId(split[0]);
                monitorUrl.setInstanceId(split[1]);
            } else {
                monitorUrl.setAppId(appIns);
            }
            monitorUrl.setBeginTime(urlTraceLog.getBeginTime().getTime());
            monitorUrl.setEndTime(urlTraceLog.getEndTime().getTime());
            monitorUrl.setConsumeTime((int) urlTraceLog.getConsumeTime());
            monitorUrl.setParams(urlTraceLog.getRequestParams());
            monitorUrl.setUrl(urlTraceLog.getUrl());
            monitorUrl.setHasException((short) (urlTraceLog.getHasException() ? 1 : 0));
            monitorUrl.setUrlTraceId(urlTraceLog.getId());

            List<MethodTraceLog> methodTraceLogList = urlTraceLog.getMethodTraceLogList();
            if (methodTraceLogList != null) {
                List<MethodLog> methodLogs = new ArrayList<MethodLog>(methodTraceLogList.size());
                for (MethodTraceLog methodTraceLog : methodTraceLogList) {
                    MethodLog methodLog = new MethodLog();
                    methodLog.setUrlTraceId(methodTraceLog.getUrlTraceLogId());
                    methodLog.setBeginTime(methodTraceLog.getBeginTime().getTime());
                    methodLog.setEndTime(methodTraceLog.getEndTime().getTime());
                    methodLog.setConsumeTime((int) methodTraceLog.getConsumeTime());
                    methodLog.setClassName(methodTraceLog.getClassName());
                    methodLog.setMethodName(methodTraceLog.getMethodName());
                    methodLog.setMethodTraceId(methodTraceLog.getId());
                    methodLog.setParam(methodTraceLog.getInParam());
                    methodLog.setResult(methodTraceLog.getOutParam());
                    methodLogs.add(methodLog);
                }
                monitorUrl.setMethodCount(methodTraceLogList.size());
                try {
                    monitorUrlHBaseDAO.saveMethodUrlLog(methodLogs, monitorUrl.getBeginTime());
                } catch (Exception e) {
                    LOGGER.errorLog(ModuleEnum.METRIC, "MonitorConsumer.saveMethodUrlLog", null, null, e);
                    System.exit(1);
                }
            }
            try {
                monitorUrlHBaseDAO.saveMonitorUrlLog(monitorUrl);
            } catch (Exception e) {
                LOGGER.errorLog(ModuleEnum.METRIC, "MonitorConsumer.saveMonitorUrlLog", null, null, e);
                System.exit(1);
            }
        }
        System.out.println("trace log size == >"+traceLog.getUrlTraceLogs().size());

        return true;
    }
}
