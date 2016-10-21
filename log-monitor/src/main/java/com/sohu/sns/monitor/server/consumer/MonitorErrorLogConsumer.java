package com.sohu.sns.monitor.server.consumer;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.sohu.sns.common.utils.json.JsonMapper;
import com.sohu.sns.monitor.bucket.TimeoutBucket;
import com.sohu.sns.monitor.enums.ErrorLogFields;
import com.sohu.sns.monitor.model.ErrorLog;
import com.sohu.snscommon.dbcluster.service.MysqlClusterService;
import com.sohu.snscommon.dbcluster.service.exception.MysqlClusterException;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Created by Gary on 2015/10/19.
 */
public class MonitorErrorLogConsumer implements Function<byte[], Boolean> {

    private static final Joiner JOINER = Joiner.on("_").skipNulls();

    private static final String INSERT_DATA = "replace into error_logs set appId = ?, instanceId = ?, " +
            "moduleName = ?, method = ?, param = ?, returnValue = ?, exceptionName = ?, exceptionDesc = ?, " +
            "stackTrace = ?, updateTime = now()";

    private JsonMapper jsonMapper = JsonMapper.nonDefaultMapper();

    private MysqlClusterService mysqlClusterService;
    private Set<String> timeoutTypes;
    private Set<String> methodsTypes;

    public MonitorErrorLogConsumer(MysqlClusterService mysqlClusterService, Set<String> timeoutTypes, Set<String> methodsTypes) {
        this.mysqlClusterService = mysqlClusterService;
        this.timeoutTypes = (null == timeoutTypes ? new HashSet<String>() : timeoutTypes);
        this.methodsTypes = methodsTypes;
    }

    @Nullable
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
     * @param msg
     * @throws MysqlClusterException
     */
    private void handle(String msg) throws Exception {
        if(Strings.isNullOrEmpty(msg)) return;
        Map<String, String> msgMap = jsonMapper.fromJson(msg, HashMap.class);
        if(null == msgMap || msgMap.isEmpty()) return;
        String[] arr = StringUtils.split(msgMap.get(ErrorLogFields.APP_ID.getName()), "_");
        if(2 != arr.length) return;
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

//       ErrorLogBucket.insertData(errorLog);

        /**超时统计*/
        if(timeoutTypes.contains(errorLog.getExceptionName())) {
            TimeoutBucket.insertData(JOINER.join(errorLog.getAppId(), errorLog.getModule(), errorLog.getMethod()));
        }

        /**特定方法短信监控**/
        String method = JOINER.join(errorLog.getAppId(), errorLog.getModule(), errorLog.getMethod());
        if(methodsTypes.contains(method)) {
            TimeoutBucket.insertData(method);
        }

//        saveToDB(errorLog);
    }

    /**
     * 将错误日志保存到数据库
     * @param errorLog
     * @throws MysqlClusterException
     */
    private void saveToDB(ErrorLog errorLog) throws Exception {
        JdbcTemplate writeJdbcTemplate = mysqlClusterService.getWriteJdbcTemplate(null);
        writeJdbcTemplate.update(INSERT_DATA, errorLog.getAppId(), errorLog.getInstanceId(),
                errorLog.getModule(), errorLog.getMethod(), errorLog.getParam(),
                errorLog.getReturnValue(), errorLog.getExceptionName(), errorLog.getExceptionDesc(),
                errorLog.getStackTrace());
    }
}
