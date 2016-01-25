package com.sohu.sns.monitor.server.consumer;

import com.google.common.base.Function;
import com.sohu.sns.common.utils.json.JsonMapper;
import com.sohu.sns.monitor.bucket.ErrorLogBucket;
import com.sohu.sns.monitor.bucket.TimeoutBucket;
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

    private static final String INSERT_DATA = "replace into error_logs set appId = ?, instanceId = ?, " +
            "moduleName = ?, method = ?, param = ?, returnValue = ?, exceptionName = ?, exceptionDesc = ?, stackTrace = ?, updateTime = now()";

    private JsonMapper jsonMapper = JsonMapper.nonDefaultMapper();

    private MysqlClusterService mysqlClusterService;
    private List<String> exceptionList;

    public MonitorErrorLogConsumer(MysqlClusterService mysqlClusterService, List<String> exceptionList) {
        this.mysqlClusterService = mysqlClusterService;
        this.exceptionList = exceptionList;
    }

    @Nullable
    @Override
    public Boolean apply(byte[] bytes) {
        String msg = null;
        try {
            msg = new String(bytes, "UTF-8");
//            LOGGER.buziLog(ModuleEnum.MONITOR_SERVICE, "applyErrorLog", msg, null);
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
        Map<String, Object> msgMap = jsonMapper.fromJson(msg, HashMap.class);
        if(null == msgMap || msgMap.isEmpty()) {
            return;
        }
        String[] arr = StringUtils.split((String) msgMap.get("appId"), "_");
        if(2 != arr.length) {
            return;
        }
        ErrorLog errorLog = new ErrorLog();
        errorLog.setAppId(arr[0].replaceAll("\"", ""));
        errorLog.setInstanceId(arr[1].replaceAll("\"", ""));
        errorLog.setModule((String) msgMap.get("module"));
        errorLog.setMethod((String) msgMap.get("method"));
        errorLog.setParam((String) msgMap.get("param"));
        errorLog.setReturnValue((String) msgMap.get("returnValue"));
        errorLog.setExceptionName((String) msgMap.get("exceptionName"));
        errorLog.setExceptionDesc((String) msgMap.get("exceptionDesc"));
        errorLog.setStackTrace((String) msgMap.get("stackTrace"));
        errorLog.setTime(new Date());

        ErrorLogBucket.insertData(errorLog);

        /**超时统计*/
        if(exceptionList.contains(errorLog.getExceptionName())) {
            TimeoutBucket.insertData(errorLog.getAppId()+"_"+errorLog.getModule()+"_"+errorLog.getMethod());
        }

        saveToDB(errorLog);
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
