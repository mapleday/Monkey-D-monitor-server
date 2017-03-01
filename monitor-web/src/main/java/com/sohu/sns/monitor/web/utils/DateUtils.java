package com.sohu.sns.monitor.web.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Gary Chan on 2016/4/4.
 */
public class DateUtils {

    public static String getCurrentTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public static String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }
}
