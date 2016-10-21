package com.sohu.sns.monitor.constant;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by morgan on 15/10/14.
 */
public final class TableConstant {

    public static String STAT_COL_FML = "stat_col_fml";
    public static String LOG_COL_FML = "log_col_fml";

    public static String NAMESPACE = "sns_monitor";
    public static String DAILY_URL_LOG_PREFIX = "%s:url_log_%s";
    public static String DAILY_METHOD_LOG_PREFIX = "%s:method_log_%s";

    public static String MINUTE_URL_STAT_PREFIX = "%s:min_url_stat_%s";
    public static String HOUR_URL_STAT_PREFIX = "%s:hou_url_stat_%s";
    public static String DAY_URL_STAT_PREFIX = "%s:day_url_stat";

    public static String MINUTE_METHOD_STAT_PREFIX = "%s:min_method_stat_%s";
    public static String HOUR_METHOD_STAT_PREFIX = "%s:hou_method_stat_%s";
    public static String DAY_METHOD_STAT_PREFIX = "%s:day_method_stat_%s";

    /**
     * 根据时间获取url日志表的表名
     * @param time
     * @return
     */
    public static String getDailyUrlLogTable(long time) {
        return String.format(DAILY_URL_LOG_PREFIX, NAMESPACE, getDayStr(time));
    }

    /**
     * 根据时间获取method日志表的表名
     * @param time
     * @return
     */
    public static String getDailyMethodLogTable(long time) {
        return String.format(DAILY_METHOD_LOG_PREFIX, NAMESPACE, getDayStr(time));
    }

    private static String getDayStr(long time) {
        return new SimpleDateFormat("yyyyMMdd").format(new Date(time));
    }

}
