package com.sohu.sns.monitor.log.server.consumer;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.sohu.sns.common.utils.json.JsonMapper;
import com.sohu.sns.monitor.log.bucket.TimeoutBucket;
import com.sohu.sns.monitor.log.enums.ErrorLogFields;
import com.sohu.sns.monitor.log.bucket.TimeoutBucket;
import com.sohu.sns.monitor.log.enums.ErrorLogFields;
import com.sohu.sns.monitor.log.model.ErrorLog;
import com.sohu.sns.monitor.log.model.ErrorLog;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Gary on 2015/10/19.
 */
public class MonitorErrorLogConsumer implements Function<byte[], Boolean> {

    private static final Joiner JOINER = Joiner.on("_").skipNulls();

    private JsonMapper jsonMapper = JsonMapper.nonDefaultMapper();

    @Override
    public Boolean apply(byte[] bytes) {
        String msg = null;
        try {
            msg = new String(bytes, "UTF-8");
            handle(msg);
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "apply.msg.handleMsg", msg, null, e);
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 处理所获得的错误日志信息
     *
     * @param msg
     */
    private void handle(String msg) throws Exception {
        if (Strings.isNullOrEmpty(msg)) return;
        Map<String, String> msgMap = jsonMapper.fromJson(msg, HashMap.class);
        if (null == msgMap || msgMap.isEmpty()) return;
        String[] arr = StringUtils.split(msgMap.get(ErrorLogFields.APP_ID.getName()), "_");
        if (2 != arr.length) return;
        ErrorLog errorLog = new ErrorLog();
        errorLog.setAppId(arr[0].replaceAll("\"", ""));
        errorLog.setInstanceId(arr[1].replaceAll("\"", ""));
        errorLog.setModule(msgMap.get(ErrorLogFields.MODULE.getName()));
        errorLog.setMethod(msgMap.get(ErrorLogFields.METHOD.getName()));
        errorLog.setParam(msgMap.get(ErrorLogFields.PARAM.getName()));
        errorLog.setReturnValue(msgMap.get(ErrorLogFields.RETURN_VALUE.getName()));
        errorLog.setExceptionName(msgMap.get(ErrorLogFields.EXCEPTION_NAME.getName()));
        errorLog.setExceptionDesc(msgMap.get(ErrorLogFields.EXCEPTION_DESC.getName()));
        errorLog.setStackTrace(msgMap.get(ErrorLogFields.STACK_TRACE.getName()));
        errorLog.setTime(new Date());

        TimeoutBucket.insertData(JOINER.join(errorLog.getAppId(), errorLog.getModule(), errorLog.getMethod()));
    }

}
