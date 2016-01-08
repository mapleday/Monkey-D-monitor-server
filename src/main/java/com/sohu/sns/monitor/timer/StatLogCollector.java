package com.sohu.sns.monitor.timer;

import com.sohu.sns.monitor.model.StatLogInfo;
import com.sohu.sns.monitor.util.DateUtil;
import com.sohu.sns.monitor.util.MathsUtils;
import com.sohu.snscommon.dbcluster.service.MysqlClusterService;
import com.sohu.snscommon.dbcluster.service.exception.MysqlClusterException;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import com.sohu.snscommon.utils.http.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

/**
 * Created by Gary on 2015/12/15.
 */
@Component
public class StatLogCollector {

    private static final String EMAIL_URL = "http://sns-mail-sms.apps.sohuno.com/sendSimpleEmail";
    @Autowired
    private MysqlClusterService mysqlClusterService;

    @Value("#{myProperties[mail_subject]}")
    private String subject;

    @Value("#{myProperties[mail_to]}")
    private String mailTo;

    private static final String QUERY_STORM_RESULT = "select appId, moduleName, methodName, count(distinct(instanceId)) instanceNum, " +
            "sum(visitCount) visitCount, sum(timeoutCount) timeoutCount, sum(allCompileTime) allCompileTime from statLog_info where " +
            "updateTime >= ? and updateTime <= ? group by appId, moduleName, methodName";
    private static final String QUERY_IS_EXIST_BYDAY = "select count(1) from statLog_info_byday where appId = ? and moduleName = ? and " +
            "methodName = ? and date_str = ?";
    private static final String INSERT_STAT_LOG_BYDAY = "replace into statLog_info_byday set appId = ?, moduleName = ?, methodName = ?,  " +
            "visitCount = ?, timeoutCount = ?, date_str = ?, updateTime = now()";
    private static final String UPDATE_STAT_LOG_BYDAY = "update statlog_info_byday set visitCount = ifnull(visitCount, 0) + ?, timeoutCount = ifnull(timeoutCount, 0) + ?, " +
            "updateTime = now() where appId = ? and moduleName = ? and methodName = ? and date_str = ?";
    private static final String INSERT_STAT_LOG_BYHOUR = "replace into statLog_info_byhour set appId = ?, moduleName = ?, methodName = ?, " +
            "instanceNum = ?, visitCount = ?, timeoutCount = ?, avgCompill = ?, currentHour = ?, date_str = ?, updateTime = now()";
    private static final String DELETE_RECORD = "delete from statLog_info where last_update >= ? and last_update <= ?";
    private static final String QUERY_HISTORY_VISITCOUNT = "select visitCount from statlog_info_byhour where appId = ? and moduleName = ? and methodName = ? and " +
            "currentHour = ? and updateTime >= ? order by date_str desc";
    private static final String VISIT_EXCEPTION = "%s_%s_%s, 访问次数:%d \n";
    private static final String EMAIL_CONTENT = "你好，日期:%s, %d时，以下接口访问次数异常,请着重查看：\n";


    public void handle() throws MysqlClusterException, ParseException {

        System.out.println("statLog collector begin ...  time : " + DateUtil.getCurrentTime());
        JdbcTemplate readJdbcTemplate = mysqlClusterService.getReadJdbcTemplate(null);
        JdbcTemplate writeJdbcTemplate = mysqlClusterService.getWriteJdbcTemplate(null);
        String beginTime = DateUtil.getBeforeCurrentHour(0);
        String endTime = DateUtil.getBeforeCurrentHour(1);
        String currentDate = DateUtil.getCollectDate();
        int beforeCurrentHour = DateUtil.getHourBefore();
        Date beginDate = DateUtil.getBeginDate();   //获取从当前时间开始往前推30天
        StringBuilder sb = new StringBuilder();

        try {
            List statLogList = readJdbcTemplate.query(QUERY_STORM_RESULT, new StormResultMapper(), beginTime, endTime);

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

                /**预测是不是访问异常**/
                List<Integer> visitCountList = readJdbcTemplate.query(QUERY_HISTORY_VISITCOUNT, new AlarmVisitCountMapper(), statLogInfo.getAppId(),
                        statLogInfo.getModuleName(), statLogInfo.getMethodName(), beforeCurrentHour, beginDate);
                Collections.sort(visitCountList);
                List<Double> result = MathsUtils.getStatus(visitCountList);
                if(null != result) {
                    if(statLogInfo.getVisitCount() > result.get(0) || statLogInfo.getVisitCount() < result.get(1)) {
                        sb.append(String.format(VISIT_EXCEPTION, statLogInfo.getAppId(), statLogInfo.getModuleName(),statLogInfo.getMethodName(),
                                statLogInfo.getVisitCount()));
                    }
                }

                /**插入每个小时的记录**/
                writeJdbcTemplate.update(INSERT_STAT_LOG_BYHOUR, statLogInfo.getAppId(), statLogInfo.getModuleName(), statLogInfo.getMethodName(),
                        statLogInfo.getInstanceNum(), statLogInfo.getVisitCount(), statLogInfo.getTimeoutCount(),
                        statLogInfo.getAllCompileTime()/statLogInfo.getVisitCount(), beforeCurrentHour, currentDate);
            }

            /**发送告警邮件**/
            if(0 != sb.length()) {
                String content = String.format(EMAIL_CONTENT, currentDate, beforeCurrentHour) + sb.toString();
                Map<String, String> map = new HashMap<String, String>();
                map.put("subject", subject);
                map.put("text", content);
                map.put("to", mailTo);
                new HttpClientUtil().postByUtf(EMAIL_URL, map, null);
            }
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "statLog.collector", null, null, e);
            e.printStackTrace();
        } finally {
            writeJdbcTemplate.update(DELETE_RECORD, beginTime, endTime);
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

    /**
     * 查询历史访问次数，以便分析报警
     */
    private class AlarmVisitCountMapper implements RowMapper<Integer> {
        @Override
        public Integer mapRow(ResultSet resultSet, int i) throws SQLException {
            return resultSet.getInt("visitCount");
        }
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMailTo() {
        return mailTo;
    }

    public void setMailTo(String mailTo) {
        this.mailTo = mailTo;
    }
}
