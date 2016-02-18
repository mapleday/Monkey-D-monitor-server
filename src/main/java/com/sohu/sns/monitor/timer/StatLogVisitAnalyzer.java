package com.sohu.sns.monitor.timer;

import com.sohu.sns.monitor.model.ExceptionValue;
import com.sohu.sns.monitor.model.StatLogInfo;
import com.sohu.sns.monitor.util.DateUtil;
import com.sohu.sns.monitor.util.MathsUtils;
import com.sohu.snscommon.dbcluster.service.MysqlClusterService;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Gary on 2015/12/15.
 */
@Component
public class StatLogVisitAnalyzer {

    private static final String EMAIL_URL = "http://sns-mail-sms.apps.sohuno.com/sendSimpleEmail";
    @Autowired
    private MysqlClusterService mysqlClusterService;

    @Value("#{myProperties[mail_subject]}")
    private String subject;

    @Value("#{myProperties[mail_to]}")
    private String mailTo;

    private static String dateStr = DateUtil.getCurrentDate();
    private static Integer hour = DateUtil.getCurrentHour();
    private static Integer period = DateUtil.getCurrentPeriod();

    private static final String QUERY_RECORD_BY_MIN = "select * from statlog_info_bymin where date_str = ? and currentHour = ? and currentPeriod = ?";
    private static final String QUERY_HISTORY_VISITCOUNT = "select visitCount from statlog_info_bymin where appId = ? and moduleName = ? " +
            "and methodName = ? and currentHour = ? and currentPeriod = ? and updateTime < ? and updateTime >= ?";
    private static final String VISIT_EXCEPTION = "%s_%s_%s, 访问次数:%d, 历史平均访问次数:%d, 最高次数:%d, 最低次数:%d\n\n";
    private static final String EMAIL_CONTENT = "你好，日期:%s, %d时, %s分钟内，以下接口访问次数异常,请着重查看：\n\n";

    public void handle() {

        String currentDateStr = DateUtil.getCurrentDate();
        Integer currentHour = DateUtil.getCurrentHour();
        Integer currentPeriod = DateUtil.getCurrentPeriod();

        /**因为storm是每五分钟向数据库中更新一次数据，
         * 异常访问也是五分钟触发一次
         * 为防止storm数据还没有更新完毕，程序休眠一分钟后执行**/
        try {
            Thread.currentThread().sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("statLog visit analyser begin ...  time : " + DateUtil.getCurrentTime());
        try {
            JdbcTemplate readJdbcTemplate = mysqlClusterService.getReadJdbcTemplate(null);
            Date beginDate = DateUtil.getBeginDate(-60);   //获取从当前时间开始往前推60天
            Date endDate = DateUtil.getBeginDate(0); //前一天
            List<ExceptionValue> exceptionValues = new LinkedList<ExceptionValue>();
            //当前时间段访问请求数
            List statLogList = readJdbcTemplate.query(QUERY_RECORD_BY_MIN, new ResultMapper(), dateStr, hour, period);
            for(Object obj : statLogList) {
                StatLogInfo statLogInfo = (StatLogInfo) obj;
                /**查看历史访问记录，预测是不是访问异常**/
                List<Integer> visitCountList = readJdbcTemplate.query(QUERY_HISTORY_VISITCOUNT, new AlarmVisitCountMapper(), statLogInfo.getAppId(),
                        statLogInfo.getModuleName(), statLogInfo.getMethodName(), hour, period, endDate, beginDate);
                Collections.sort(visitCountList);
                ExceptionValue result = MathsUtils.getStatus(visitCountList);
                if(null != result) {
                    if(statLogInfo.getVisitCount() > result.getMaxVisitCount() || statLogInfo.getVisitCount() < result.getMinVisitCount()) {
                        result.setAppId(statLogInfo.getAppId());
                        result.setModuleName(statLogInfo.getModuleName());
                        result.setMethodName(statLogInfo.getMethodName());
                        result.setVisitCount(statLogInfo.getVisitCount().intValue());
                        exceptionValues.add(result);
                    }
                }
            }
            Collections.sort(exceptionValues, new ExceptionValueComparator());
            StringBuilder sb = new StringBuilder();
            String miniute;
            if(0 != exceptionValues.size()) {
                switch (period) {
                    case 1 : miniute = "0-5"; break;
                    case 2 : miniute = "5-10"; break;
                    case 3 : miniute = "10-15"; break;
                    case 4 : miniute = "15-20"; break;
                    case 5 : miniute = "20-25"; break;
                    case 6 : miniute = "25-30"; break;
                    case 7 : miniute = "30-35"; break;
                    case 8 : miniute = "35-40"; break;
                    case 9 : miniute = "40-45"; break;
                    case 10 : miniute = "45-50"; break;
                    case 11 : miniute = "50-55"; break;
                    case 12 : miniute = "55-60"; break;
                    default : miniute = "unknown"; break;
                }
                sb.append(String.format(EMAIL_CONTENT, dateStr, hour, miniute));
            }
            for(ExceptionValue exceptionValue : exceptionValues) {
                sb.append(String.format(VISIT_EXCEPTION, exceptionValue.getAppId(), exceptionValue.getModuleName(),
                        exceptionValue.getMethodName(), exceptionValue.getVisitCount(), exceptionValue.getAvgVisitCount(),
                        exceptionValue.getMaxVisitCount(), exceptionValue.getMinVisitCount()));
            }

            /**发送告警邮件**/
            if(0 != sb.length()) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("subject", subject);
                map.put("text", sb.toString());
                map.put("to", mailTo);
                //2016-2-18 11:11:00 暂时去掉发送邮件，邮件太多，没有意义，后期优化
//                new HttpClientUtil().postByUtf(EMAIL_URL, map, null);
            }

            System.out.println("statLog visit analyser end ...  time : " + DateUtil.getCurrentTime());
        } catch (Exception e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "statLog.visit.anal", null, null, e);
            e.printStackTrace();
        } finally {
            dateStr = currentDateStr;
            hour = currentHour;
            period = currentPeriod;
        }
    }

   private class ResultMapper implements RowMapper {
        @Override
        public Object mapRow(ResultSet resultSet, int i) throws SQLException {
            StatLogInfo statLogInfo = new StatLogInfo();
            statLogInfo.setAppId(resultSet.getString("appId"));
            statLogInfo.setModuleName(resultSet.getString("moduleName"));
            statLogInfo.setMethodName(resultSet.getString("methodName"));
            statLogInfo.setVisitCount(resultSet.getLong("visitCount"));
            statLogInfo.setTimeoutCount(resultSet.getLong("timeoutCount"));
            statLogInfo.setAvgCompill(resultSet.getDouble("avgCompill"));
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

    private class ExceptionValueComparator implements Comparator<ExceptionValue> {
        @Override
        public int compare(ExceptionValue o1, ExceptionValue o2) {
            if(o2.getVisitCount() > o1.getVisitCount()) {
                return 1;
            } else if(o2.getVisitCount() < o1.getVisitCount()) {
                return -1;
            } else {
                return 0;
            }
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
