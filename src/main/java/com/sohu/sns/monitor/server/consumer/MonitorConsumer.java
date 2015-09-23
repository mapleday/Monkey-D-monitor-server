package com.sohu.sns.monitor.server.consumer;

import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.google.common.base.Function;
import com.sohu.sns.monitor.agent.store.model.url.TraceLog;
import com.sohu.sns.monitor.agent.store.model.url.UrlTraceLog;
import com.sohu.sns.monitor.server.MessageProcessor;
import com.sohu.sns.monitor.util.ProtobufUtil;

/**
 * Created by morgan on 15/9/22.
 */
public class MonitorConsumer implements Function<byte[], Boolean> {
    @Override
    public Boolean apply(byte[] input) {
        byte[] data = ProtobufUtil.decompress(input);
        TraceLog traceLog = new TraceLog();
        ProtostuffIOUtil.mergeFrom(data, traceLog, MessageProcessor.getSchema());
        System.out.println(traceLog.toString());
        System.out.println("trace log size == >"+traceLog.getUrlTraceLogs().size());
        //
        return true;
    }
}
