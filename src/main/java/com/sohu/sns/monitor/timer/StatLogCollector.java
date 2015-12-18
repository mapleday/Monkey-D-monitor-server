package com.sohu.sns.monitor.timer;

import com.sohu.sns.monitor.model.StatLogInfo;
import com.sohu.sns.monitor.util.DateUtil;
import com.sohu.snscommon.dbcluster.service.MysqlClusterService;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

/**
 * Created by Gary on 2015/12/15.
 */
@Component
public class StatLogCollector {

    @Autowired
    private MysqlClusterService mysqlClusterService;
    private static final String UPDATE_FLAG = "update statlog_status set status = ? where id = 1";
    private static final String QUERY_FLAG = "select status from statlog_status where id = 1";
    private static final String QUERY_STORM_RESULT = "select appId, moduleName, methodName, count(distinct(instanceId)) instanceNum, " +
            "sum(visitCount) visitCount, sum(timeoutCount) timeoutCount, sum(allCompileTime) allCompileTime from statLog_info where " +
            "updateTime >= ? and updateTime <= ? group by appId, moduleName, methodName";
    private static final String QUERY_STORM_RESULT_INSTANCE = "select instanceId, appId, moduleName, methodName, sum(visitCount) visitCount, " +
            "sum(timeoutCount) timeoutCount, sum(allCompileTime) allCompileTime from statLog_info where updateTime >= ? and updateTime <= ? " +
            "group by instanceId, appId, moduleName, methodName";
    private static final String QUERY_IS_EXIST_BYDAY = "select count(1) from statLog_info_byday where appId = ? and moduleName = ? and " +
            "methodName = ? and date_str = ?";
    private static final String INSERT_STAT_LOG_BYDAY = "replace into statLog_info_byday set appId = ?, moduleName = ?, methodName = ?,  " +
            "visitCount = ?, timeoutCount = ?, date_str = ?, updateTime = now()";
    private static final String UPDATE_STAT_LOG_BYDAY = "update statlog_info_byday set visitCount = ifnull(visitCount, 0) + ?, timeoutCount = ifnull(timeoutCount, 0) + ?, " +
            "updateTime = now() where appId = ? and moduleName = ? and methodName = ? and date_str = ?";
    private static final String INSERT_STAT_LOG_BYHOUR = "replace into statLog_info_byhour set appId = ?, moduleName = ?, methodName = ?, " +
            "instanceNum = ?, visitCount = ?, timeoutCount = ?, avgCompill = ?, currentHour = ?, date_str = ?, updateTime = now()";
    private static final String INSERT_STAT_LOG_BYHOUR_INSTANCE = "replace into statLog_info_byhour_instanceid set instanceId = ?, appId = ?, " +
            "moduleName = ?, methodName = ?, visitCount = ?, timeoutCount = ?, avgCompill = ?, currentHour = ?, date_str = ?, updateTime = now()";


    @Scheduled(cron = "0 0 0/1 * * ? ")
    //@Scheduled(cron = "0/30 * * * * ? ")
    public void handle() {
        try {
            JdbcTemplate readJdbcTemplate = mysqlClusterService.getReadJdbcTemplate(null);
            JdbcTemplate writeJdbcTemplate = mysqlClusterService.getWriteJdbcTemplate(null);

            int random = new Random().nextInt(10000);
            writeJdbcTemplate.update(UPDATE_FLAG, random);
            Thread.currentThread().sleep(35000);

            Long flag = readJdbcTemplate.queryForObject(QUERY_FLAG, Long.class);
            if(flag != random) {
                return;
            }
            System.out.println("statLog collector begin ...  time : " + DateUtil.getCurrentTime());

            String beginTime = DateUtil.getBeforeCurrentHour(0);
            String endTime = DateUtil.getBeforeCurrentHour(1);
            String currentDate = DateUtil.getCollectDate();
            int beforeCurrentHour = DateUtil.getHourBefore();
            List statLogList = readJdbcTemplate.query(QUERY_STORM_RESULT, new StormResultMapper(), beginTime, endTime);
            List statLogListByInstance = readJdbcTemplate.query(QUERY_STORM_RESULT_INSTANCE, new StormResultByInstanceMapper(), beginTime, endTime);

            for(Object obj : statLogList) {
                StatLogInfo statLogInfo = (StatLogInfo) obj;

                Long countByDay = readJdbcTemplate.queryForObject(QUERY_IS_EXIST_BYDAY, Long.class, statLogInfo.getAppId(),
                        statLogInfo.getModuleName(), statLogInfo.getMethodName(), currentDate);
                if(0 == countByDay) {

                    /** 插入新的统计*/
                    writeJdbcTemplate.update(INSERT_STAT_LOG_BYDAY, statLogInfo.getAppId(), statLogInfo.getModuleName(), statLogInfo.getMethodName(),
                            statLogInfo.getVisitCount(), statLogInfo.getTimeoutCount(), currentDate);
                } else {

                    /** 更新已经存在的记录*/
                    writeJdbcTemplate.update(UPDATE_STAT_LOG_BYDAY, statLogInfo.getVisitCount(), statLogInfo.getTimeoutCount(), statLogInfo.getAppId(),
                            statLogInfo.getModuleName(), statLogInfo.getMethodName(), currentDate);
                }

                /**插入每个小时的记录**/
                writeJdbcTemplate.update(INSERT_STAT_LOG_BYHOUR, statLogInfo.getAppId(), statLogInfo.getModuleName(), statLogInfo.getMethodName(),
                        statLogInfo.getInstanceNum(), statLogInfo.getVisitCount(), statLogInfo.getTimeoutCount(),
                        statLogInfo.getAllCompileTime()/statLogInfo.getVisitCount(), beforeCurrentHour, currentDate);
            }

            for(Object obj : statLogListByInstance) {
                StatLogInfo statLogInfo = (StatLogInfo) obj;

                writeJdbcTemplate.update(INSERT_STAT_LOG_BYHOUR_INSTANCE, statLogInfo.getInstanceId(), statLogInfo.getAppId(), statLogInfo.getModuleName(),
                        statLogInfo.getMethodName(), statLogInfo.getVisitCount(), statLogInfo.getTimeoutCount(),
                        statLogInfo.getAllCompileTime()/statLogInfo.getVisitCount(), beforeCurrentHour, currentDate);
            }
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "statLog.collector", null, null, e);
            e.printStackTrace();
        }

    }

    private class StormResultMapper implements RowMapper {

        @Override
        public Object mapRow(ResultSet resultSet, int i) throws SQLException {
            StatLogInfo statLogInfo = new StatLogInfo();
            statLogInfo.setAppId(resultSet.getString("appId"));
            statLogInfo.setModuleName(resultSet.getString("moduleName"));
            statLogInfo.setMethodName(resultSet.getString("methodName"));
            statLogInfo.setVisitCount(resultSet.getLong("visitCount"));
            statLogInfo.setTimeoutCount(resultSet.getLong("timeoutCount"));
            statLogInfo.setInstanceNum(resultSet.getInt("instanceNum"));
            statLogInfo.setAllCompileTime(resultSet.getLong("allCompileTime"));
            return statLogInfo;
        }
    }

    private class StormResultByInstanceMapper implements RowMapper {

        @Override
        public Object mapRow(ResultSet resultSet, int i) throws SQLException {
            StatLogInfo statLogInfo = new StatLogInfo();
            statLogInfo.setAppId(resultSet.getString("appId"));
            statLogInfo.setInstanceId(resultSet.getString("instanceId"));
            statLogInfo.setModuleName(resultSet.getString("moduleName"));
            statLogInfo.setMethodName(resultSet.getString("methodName"));
            statLogInfo.setVisitCount(resultSet.getLong("visitCount"));
            statLogInfo.setTimeoutCount(resultSet.getLong("timeoutCount"));
            statLogInfo.setAllCompileTime(resultSet.getLong("allCompileTime"));
            return statLogInfo;
        }
    }

}
