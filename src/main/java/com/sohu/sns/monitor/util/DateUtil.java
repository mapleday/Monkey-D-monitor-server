package com.sohu.sns.monitor.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Gary on 2015/11/12.
 */
public class DateUtil {
    /**
     * 获得小时
     * @return
     */
    public static String getHour() {
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
    private static String getCurrentHourStr(int currentHour) {
        String current;
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
            default :
                current = null;
        }
        return current;
    }

    /**
     * 得到当前的日期
     * @return
     */
    public static String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    /**
     * 得到当前的时间
     * @return
     */
    public static String getCurrentTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

}
