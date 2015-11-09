package com.sohu.sns.monitor.timer;

import com.sohu.sns.monitor.bucket.ApiStatusBucket;
import com.sohu.sns.monitor.model.ApiStatusCount;
import com.sohu.snscommon.dbcluster.service.MysqlClusterService;
import com.sohu.snscommon.dbcluster.service.exception.MysqlClusterException;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Gary on 2015/11/6.
 */
@Component
public class ApiCollecterProcessor {

    private static final String QUERY_PATH = "select count(1) from api_method_to_path where methodName = ?";
    private static final String IS_EXIST_METHOD = "select count(1) from api_use_count where methodName = ? and date_str = ?";
    private static final String INSERT_METHOD = "replace into api_method_to_path set methodName = ?, pathName = ? ";
    private static final String INSERT_API_USE_RECORD = "replace into api_use_count set methodName = ?, " +
            "allCount = ?, %s = ?, date_str = ?";
    private static final String UPDATE_API_USE_RECORD = "update api_use_count set allCount = ifnull(allCount, 0) + ?, %s = ? where methodName = ? and " +
            "date_str = ?";
    private static final String IS_EXIST_TIMEOUT = "select count(1) from api_timeout_count where methodName = ? and date_str = ?";
    private static final String INSERT_TIMEOUT_RECORD = "replace into api_timeout_count set methodName = ?, " +
            "allCount = ?, %s = ?, date_str = ?";
    private static final String UPDATE_TIMEOUT_RECORD = "update api_timeout_count set allCount = ifnull(allCount, 0) + ?, %s = ? where methodName = ? and " +
            "date_str = ?";
    @Autowired
    private MysqlClusterService mysqlClusterService;

    @Scheduled(cron = "0 0/60 * * * ? ")
    //@Scheduled(cron = "0/60 * * * * ? ")
    public void process() {
        try {
            System.out.println("process api_status begin, time :" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            String hour = getHour();
            String date_str = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            JdbcTemplate readJdbcTemplate = mysqlClusterService.getReadJdbcTemplate(null);
            JdbcTemplate writeJdbcTemplate = mysqlClusterService.getWriteJdbcTemplate(null);
            Map<String, ApiStatusCount> lastBucket = ApiStatusBucket.exchange();
            Iterator<Map.Entry<String, ApiStatusCount>> iter = lastBucket.entrySet().iterator();
            while(iter.hasNext()) {
                Map.Entry<String, ApiStatusCount> entry = iter.next();
                if(0 == readJdbcTemplate.queryForObject(QUERY_PATH, Long.class, entry.getKey())) {
                    writeJdbcTemplate.update(INSERT_METHOD, entry.getKey(), "unknown");
                }

                /*更新访问数量*/
                long useCount = readJdbcTemplate.queryForObject(IS_EXIST_METHOD, Long.class, entry.getKey(), date_str);
                if(0 == useCount) {
                    String insertSql = String.format(INSERT_API_USE_RECORD, hour);
                    writeJdbcTemplate.update(insertSql, entry.getKey(), entry.getValue().getUseCount(),
                            entry.getValue().getUseCount(), date_str);
                } else {
                    String updateSql = String.format(UPDATE_API_USE_RECORD, hour);
                    writeJdbcTemplate.update(updateSql, entry.getValue().getUseCount(), entry.getValue().getUseCount(),
                            entry.getKey(), date_str);
                }

                /*更新超过1秒的访问数量*/
                long timeoutCount = readJdbcTemplate.queryForObject(IS_EXIST_TIMEOUT, Long.class, entry.getKey(), date_str);
                if(0 == timeoutCount) {
                    String insertSql = String.format(INSERT_TIMEOUT_RECORD, hour);
                    writeJdbcTemplate.update(insertSql, entry.getKey(), entry.getValue().getTimeOutCount(),
                            entry.getValue().getTimeOutCount(), date_str);
                } else {
                    String updateSql = String.format(UPDATE_TIMEOUT_RECORD, hour);
                    writeJdbcTemplate.update(updateSql, entry.getValue().getTimeOutCount(), entry.getValue().getTimeOutCount(),
                            entry.getKey(), date_str);
                }
            }
        } catch (MysqlClusterException e) {
            LOGGER.errorLog(ModuleEnum.MONITOR_SERVICE, "sendApiStatusEmail", null, null, e);
        }
    }

    /**
     * 获得小时
     * @return
     */
    private String getHour() {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, -5);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        return getCurrentHourStr(hour);
    }

    /**
     * 将小时装换成相应的字符串
     * @param currentHour
     * @return
     */
    private String getCurrentHourStr(int currentHour) {
        String current = null;
        switch (currentHour) {
            case 0 :
                current = "one";
                break;
            case 1 :
                current = "two";
                break;
            case 2 :
                current = "three";
                break;
            case 3 :
                current = "four";
                break;
            case 4 :
                current = "five";
                break;
            case 5 :
                current = "six";
                break;
            case 6 :
                current = "seven";
                break;
            case 7 :
                current = "eight";
                break;
            case 8 :
                current = "nine";
                break;
            case 9 :
                current = "ten";
                break;
            case 10 :
                current = "eleven";
                break;
            case 11 :
                current = "twelve";
                break;
            case 12 :
                current = "thirteen";
                break;
            case 13 :
                current = "fourteen";
                break;
            case 14 :
                current = "fifteen";
                break;
            case 15 :
                current = "sixteen";
                break;
            case 16 :
                current = "seventeen";
                break;
            case 17 :
                current = "eighteen";
                break;
            case 18 :
                current = "nineteen";
                break;
            case 19 :
                current = "twenty";
                break;
            case 20 :
                current = "twentyone";
                break;
            case 21 :
                current = "twentytwo";
                break;
            case 22 :
                current = "twentythree";
                break;
            case 23 :
                current = "twentyfour";
                break;
        }
        return current;
    }

    private class PathNameMapper implements RowMapper {
        @Override
        public Object mapRow(ResultSet resultSet, int i) throws SQLException {
            String result = resultSet.getString("pathName");
            return result;
        }
    }
}
