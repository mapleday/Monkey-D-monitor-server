package com.sohu.sns.monitor.timer;

import com.sohu.sns.monitor.bucket.ApiStatusBucket;
import com.sohu.sns.monitor.model.ApiStatus;
import com.sohu.sns.monitor.util.DateUtil;
import com.sohu.snscommon.dbcluster.service.MysqlClusterService;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by Gary on 2015/11/6.
 */
@Component
public class ApiStatusProcessor {

    @Autowired
    private MysqlClusterService mysqlClusterService;

    /**查询、插入等方法名，模块名称，URL对应名称表*/
    private static final String IS_EXIST_METHOD_TO_PATH = "select count(1) from api_method_to_path where moduleName = ? and methodName = ?";
    private static final String INSERT_UNIQUE_METHOD = "replace into api_method_to_path set moduleName = ?, methodName = ?, pathName = ? ";

    /**查询，插入，更新API的使用情况*/
    private static final String IS_EXIST_API_STATUS = "select count(1) from api_status_count where moduleName = ? and methodName = ? and date_str = ?";
    private static final String INSERT_API_STATUS_RECORD = "replace into api_status_count set moduleName = ?, methodName = ?, " +
            "allCount = ?, %s = ?, date_str = ?";
    private static final String UPDATE_API_STATUS_RECORD = "update api_status_count set allCount = ifnull(allCount, 0) + ?, %s = ifnull(%s, 0)+? where " +
            "moduleName = ? and methodName = ? and date_str = ?";

    /**查询，插入，更新API的超时情况*/
    private static final String IS_EXIST_TIMEOUT = "select count(1) from api_timeout_count where moduleName = ? and methodName = ? and date_str = ?";
    private static final String INSERT_TIMEOUT_RECORD = "replace into api_timeout_count set moduleName = ?, methodName = ?, " +
            "allCount = ?, %s = ?, date_str = ?";
    private static final String UPDATE_TIMEOUT_RECORD = "update api_timeout_count set allCount = ifnull(allCount, 0) + ?, %s = ifnull(%s, 0)+? where " +
            "moduleName = ? and methodName = ? and date_str = ?";


    @Scheduled(cron = "0 0/15 * * * ? ")
    //@Scheduled(cron = "0/60 * * * * ? ")
    public void process() {
        Map<String, ApiStatus> bucket = ApiStatusBucket.exchange();
        try {
            System.out.println("process api_status begin, time :" + DateUtil.getCurrentTime()
                    + ", bucketSize:"+bucket.size());
            if(bucket.isEmpty()) {
                return;
            }
            String hour = DateUtil.getHour();
            String date_str = DateUtil.getCurrentDate();
            JdbcTemplate readJdbcTemplate = mysqlClusterService.getReadJdbcTemplate(null);
            JdbcTemplate writeJdbcTemplate = mysqlClusterService.getWriteJdbcTemplate(null);
            Iterator<Map.Entry<String, ApiStatus>> iter = bucket.entrySet().iterator();
            while(iter.hasNext()) {
                Map.Entry<String, ApiStatus> entry = iter.next();
                if(0 == readJdbcTemplate.queryForObject(IS_EXIST_METHOD_TO_PATH, Long.class,
                        entry.getValue().getModuleName(), entry.getValue().getMethodName())) {
                    writeJdbcTemplate.update(INSERT_UNIQUE_METHOD, entry.getValue().getModuleName(),
                            entry.getValue().getMethodName(), "unknown");
                }

                /**更新访问数量*/
                long useCount = readJdbcTemplate.queryForObject(IS_EXIST_API_STATUS, Long.class,
                        entry.getValue().getModuleName(), entry.getValue().getMethodName(), date_str);
                if(0 == useCount) {
                    String insertSql = String.format(INSERT_API_STATUS_RECORD, hour);
                    writeJdbcTemplate.update(insertSql, entry.getValue().getModuleName(), entry.getValue().getMethodName(),
                            entry.getValue().getUseCount(), entry.getValue().getUseCount(), date_str);
                } else {
                    String updateSql = String.format(UPDATE_API_STATUS_RECORD, hour, hour);
                    writeJdbcTemplate.update(updateSql, entry.getValue().getUseCount(), entry.getValue().getUseCount(),
                            entry.getValue().getModuleName(), entry.getValue().getMethodName(), date_str);
                }

                /**更新超过1秒的访问数量*/
                long timeoutCount = readJdbcTemplate.queryForObject(IS_EXIST_TIMEOUT, Long.class, entry.getValue().getModuleName(),
                        entry.getValue().getMethodName(), date_str);
                if(0 == timeoutCount) {
                    String insertSql = String.format(INSERT_TIMEOUT_RECORD, hour);
                    writeJdbcTemplate.update(insertSql, entry.getValue().getModuleName(), entry.getValue().getMethodName(),
                            entry.getValue().getTimeOutCount(), entry.getValue().getTimeOutCount(), date_str);
                } else {
                    String updateSql = String.format(UPDATE_TIMEOUT_RECORD, hour, hour);
                    writeJdbcTemplate.update(updateSql, entry.getValue().getTimeOutCount(), entry.getValue().getTimeOutCount(),
                            entry.getValue().getModuleName(), entry.getValue().getMethodName(), date_str);
                }
            }
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "api_statusSaveToDB", null, null, e);
        } finally {
            bucket.clear();
        }
    }
}
