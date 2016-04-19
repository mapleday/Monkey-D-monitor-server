package com.sohu.sns.monitor.util;

/**
 * Created by Gary Chan on 2016/4/16.
 */
public class RedisEmailUtil {

    public static final String SUBJECT = "Redis异常预测";
    public static final String TIME = "<br>当前检查时间 : %s, 上次检查时间 : %s";
    public static final String VISIT_EXCEPTION = "<br>1.未能成功访问的Redis实例或uid为: %s";
    public static final String KEYS_EXCEPTION = "<br>2.keys不一致的Master-Slave结点为: %s";
    public static final String GROW_EXCEPTION = "<br>3.KEYS较上次统计增幅超过10%%的Redis实例为: %s";
    public static final String DECLINE_EXCEPTION = "<br>4.KEYS较上次统计下降10%%的Redis实例为: %s";

    private static final String BOLD_HTML = "<strong>%s</strong>";
    private static final String COLOR_HTML = "<font color=\"%s\">%s</font>";
    public static final String CRLF = "<br>";
    private static final String SPACE = "&nbsp;";

    public static String boldLine(String line) {
        if(null == line) line = "";
        return String.format(BOLD_HTML, line);
    }

    public static String colorLine(String line, String color) {
        if(null == line) line = "";
        if(null == color) color = "red";
        return String.format(COLOR_HTML, color, line);
    }

    public static String getSpace(int count) {
        if(count < 1) count = 1;
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < count; i++) {
            sb.append(SPACE);
        }
        return sb.toString();
    }
}
