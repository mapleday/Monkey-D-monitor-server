package com.sohu.sns.monitor.server.consumer;

import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.google.common.base.Function;
import com.sohu.sns.monitor.agent.store.model.url.TraceLog;
import com.sohu.sns.monitor.agent.store.model.url.UrlTraceLog;
import com.sohu.sns.monitor.bucket.MonitorUrlBucket;
import com.sohu.sns.monitor.model.MonitorUrl;
import com.sohu.sns.monitor.server.MessageProcessor;
import com.sohu.sns.monitor.util.ProtobufUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by morgan on 15/9/22.
 */
public class MonitorConsumer implements Function<byte[], Boolean> {

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
            monitorUrl.setAppId(appIns);    //appIns
            monitorUrl.setConsumeTime((int) urlTraceLog.getConsumeTime());  //url耗费的时间
            monitorUrl.setUrl(urlTraceLog.getUrl());    //url地址
            monitorUrl.setHasException((short) (urlTraceLog.getHasException() ? 1 : 0));    //是不是有异常
            monitorUrl.setMethodCount(urlTraceLog.getMethodTraceLogList().size());  // 访问的方法

            MonitorUrlBucket.insertData(monitorUrl);
        }

        System.out.println("trace log size == >" + traceLog.getUrlTraceLogs().size());
        return true;
    }
}
