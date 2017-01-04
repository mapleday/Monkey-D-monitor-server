package com.sohu.sns.monitor.log.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Gary on 2015/11/12.
 */
public class DateUtil {

    public static Integer getCurrentPeriod() {
        Double min = Double.parseDouble(new SimpleDateFormat("mm").format(new Date()));
        if(0 == min % 5.0) {
            return (int)(min/5.0 + 1);
        } else {
            return (int)Math.ceil(min/5.0);
        }
    }

    public static Integer getCurrentHour() {
        return Integer.parseInt(new SimpleDateFormat("HH").format(new Date()));
    }

    /**
     * 获得小时
     * @return
     */
    public static String getHour() {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, -1);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        return getCurrentHourStr(hour);
    }

    public static int getHourBefore() {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.HOUR_OF_DAY, -1);
        return now.get(Calendar.HOUR_OF_DAY);
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

    public static String getCollectDate() {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.HOUR_OF_DAY, -1);
        Date date = now.getTime();
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    /**
     * 得到当前的时间
     * @return
     */
    public static String getCurrentTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    /**
     * 得到当前所在的分钟
     * @return
     */
    public static String getCurrentMin() {
        String min = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
        return min + ":00";
    }

    /**
     * 获取当前时间的上一个小时的开始时间和结束时间
     * @param flag 0：上个小时的开始时间， 1：上个小时的结束时间
     * @return
     */
    public static String getBeforeCurrentHour(int flag) {
        if (flag > 1 || flag < 0) {
            return null;
        }
        Calendar now = Calendar.getInstance();
        StringBuilder stringBuilder = new StringBuilder();
        now.add(Calendar.HOUR_OF_DAY, -1);
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1;
        int day = now.get(Calendar.DAY_OF_MONTH);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        if (0 == flag) {
            return stringBuilder.append(year).append("-").append(month < 10 ? "0" + month : month).append("-")
                    .append(day < 10 ? "0" + day : day).append(" ").append(hour < 10 ? "0" + hour : hour).append(":00:00").toString();
        } else {
            return stringBuilder.append(year).append("-").append(month < 10 ? "0" + month : month).append("-")
                    .append(day < 10 ? "0" + day : day).append(" ").append(hour < 10 ? "0" + hour : hour).append(":59:59").toString();
        }
    }

    /**
     * 获取从当前时间往前推60天的开始时间
     * @return
     * @throws ParseException
     */
    public static Date getBeginDate(int delay) throws ParseException {
        StringBuilder stringBuilder = new StringBuilder();
        Calendar now = Calendar.getInstance();
        now.add(Calendar.DAY_OF_MONTH, delay);
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1;
        int day = now.get(Calendar.DAY_OF_MONTH);
        stringBuilder.append(year).append("-").append(month < 10 ? "0" + month : month).append("-")
                .append(day < 10 ? "0" + day : day).append(" 00:00:00");
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(stringBuilder.toString());
    }

    public static Integer getCollectHour() {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.HOUR_OF_DAY, -1);
        String hour = new SimpleDateFormat("HH").format(now.getTime());
        return Integer.parseInt(hour);
    }

    public static String getLastDay() {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.DAY_OF_MONTH, -1);
        return new SimpleDateFormat("yyyy-MM-dd").format(now.getTime());

    }
}
