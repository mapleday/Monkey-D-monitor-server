package com.sohu.sns.monitor.util;

/**
 * Created by Gary Chan on 2016/4/1.
 */
public class EmailStringFormatUtils {

    private static final String EMAIL_BEGIN_CONTENT = "<br><div><b><font color=\"red\"> %s : </font></b></div><br>" +
            "<table border=\"1\" cellpadding=\"0\" cellspacing=\"0\" width=\"800\" style=\"border-collapse: collapse; table-layout:fixed;\">";

    private static final String EMAIL_END_CONTENT =
            "<tr><td align=\"center\" ><b>Params</b></td><td style=\"word-wrap:break-word;\">%s</td></tr>" +
            "<tr><td align=\"center\" ><b>StackTrace</b></td><td style=\"word-wrap:break-word;\"><a href=\"%s\">点击查看</a></td></tr>" +
            "<tr><td align=\"center\"><b>出现次数</b></td><td style=\"word-wrap:break-word;\">%d</td></tr>" +
            "<tr><td colspan=\"2\">&nbsp;</td></tr>";

    public static String formatHead(String instance) {
        instance = (null==instance ? "" : instance);
        return String.format(EMAIL_BEGIN_CONTENT, instance);
    }

    public static String formatTail(String params, String stackTraceUrl, Integer times) {
        return String.format(EMAIL_END_CONTENT, params, stackTraceUrl, times);
    }

}
