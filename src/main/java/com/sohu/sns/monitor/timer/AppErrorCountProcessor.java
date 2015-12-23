package com.sohu.sns.monitor.timer;

import com.sohu.sns.monitor.util.DateUtil;
import com.sohu.snscommon.dbcluster.service.impl.MysqlClusterServiceImpl;
import com.sohu.snscommon.utils.LOGGER;
import com.sohu.snscommon.utils.constant.ModuleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Gary on 2015/11/4.
 */

@Component
public class AppErrorCountProcessor{

    @Autowired
    private MysqlClusterServiceImpl mysqlClusterService;

    private static final String QUERY_ERROR_PER_HOUR = "select appId, count(appId) count from error_logs where updateTime >= ? and updateTime <= ? group by appId";
    private static final String QUERY_IS_EXIST = "select count(1) from app_error_count_per_hour where appId = ? and date_str = ?";
    private static final String INSERT_RECORD = "replace into app_error_count_per_hour set appId = ?, allCount = ?, %s = ?, date_str = ?";
    private static final String UPDATE_RECORD = "update app_error_count_per_hour set allCount = ifnull(allCount, 0)+?, %s = ? where appId = ? and date_str = ?";

    public void process() {
        System.out.println("count app error times start ...... time :" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        JdbcTemplate readJdbcTemplate = mysqlClusterService.getReadJdbcTemplate(null);
        JdbcTemplate writeJdbcTemplate = mysqlClusterService.getWriteJdbcTemplate(null);

        String startTime = DateUtil.getBeforeCurrentHour(0);
        String endTime = DateUtil.getBeforeCurrentHour(1);
        List list = readJdbcTemplate.query(QUERY_ERROR_PER_HOUR, new PullPushCountByDayMapper(), startTime, endTime);
        if(list.isEmpty()) return;

        /**取得要统计时间的日期和小时信息**/
        String date_str = DateUtil.getCollectDate();
        String current = getCurrentHourStr(getHourBeforeThis());
        for(Object obj : list) {
            CountPair countPair = (CountPair) obj;
            Long count = readJdbcTemplate.queryForObject(QUERY_IS_EXIST, Long.class, countPair.getAppId(), date_str);
            if(0 == count) {
                String insertLog = String.format(INSERT_RECORD, current);
                writeJdbcTemplate.update(insertLog, countPair.getAppId(), countPair.getCount(), countPair.getCount(), date_str);
            } else {
                String updateLog = String.format(UPDATE_RECORD, current);
                writeJdbcTemplate.update(updateLog, countPair.getCount(), countPair.getCount(), countPair.getAppId(), date_str);
            }
        }
        LOGGER.buziLog(ModuleEnum.MONITOR_SERVICE, "countAppErrorsByHour", null, "handled errors count:" + list.size());
    }

    /**
     * 得到当前时间的前一个小时
     * @return
     */
    private int getHourBeforeThis() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -1);
        return calendar.get(Calendar.HOUR_OF_DAY);
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

    private class PullPushCountByDayMapper implements RowMapper {
        @Override
        public Object mapRow(ResultSet resultSet, int i) throws SQLException {
            CountPair countPair = new CountPair();
            countPair.setAppId(resultSet.getString("appId"));
            countPair.setCount(resultSet.getInt("count"));
            return countPair;
        }
    }

    private class CountPair {
        private String appId;
        private int count;
        public String getAppId() {
            return appId;
        }
        public void setAppId(String appId) {
            this.appId = appId;
        }
        public int getCount() {
            return count;
        }
        public void setCount(int count) {
            this.count = count;
        }
    }
}
