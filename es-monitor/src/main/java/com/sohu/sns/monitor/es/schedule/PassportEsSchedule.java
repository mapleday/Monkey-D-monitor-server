package com.sohu.sns.monitor.es.schedule;

import com.sohu.sns.monitor.common.services.MailService;
import com.sohu.sns.monitor.common.services.NotifyService;
import com.sohu.sns.monitor.es.module.PassportEsResult;
import com.sohu.sns.monitor.es.query.PassportEsAnalysis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * author:jy
 * time:17-1-18下午8:29
 */


@Component("passportEsSchedule")
public class PassportEsSchedule {
    private static long lastNotifyTime = 0;//上次预警时间

    @Autowired
    NotifyService notifyService;
    @Autowired
    MailService mailService;

    public void monitor() {
        //查询今天和昨天的最近5分钟接口调用情况
        List<PassportEsResult> results = PassportEsAnalysis.getInstance().analysisTowDayQpm(2.5f);

        StringBuilder sb = new StringBuilder("时间,接口名称,数量,昨天,总计,昨天总计");
        if (!results.isEmpty()) {
            sb.append("internal.passport.sohu.com5分钟接口\n");
            appendMonitorMsg(sb, results);
        }
        List<PassportEsResult> results2 = PassportEsAnalysis.getInstance().analysisTowDayAppKey(3.0f);
        if (!results2.isEmpty()) {
            sb.append("plus.sohuno.com5分钟appkey\n");
            appendMonitorMsg(sb, results2);
        }
        List<PassportEsResult> result3 = PassportEsAnalysis.getInstance().analysisTowDayPassportSohu(1.8f);
        if (!result3.isEmpty()) {
            sb.append("passport.sohu.com5分钟接口\n");
            appendMonitorMsg(sb, result3);
        }
        List<PassportEsResult> result4 = PassportEsAnalysis.getInstance().analysisTowDayPlusSohu(1.8f);
        if (!result4.isEmpty()) {
            sb.append("plus.sohu.com5分钟接口\n");
            appendMonitorMsg(sb, result4);
        }

//        System.out.println(sb.toString());
        notifyService.sendNotifyToPersonGroup(sb.toString(),"passport");

        StringBuilder content = new StringBuilder(HTML_HEAD);
        content.append(genHtmlContent("internal.passport.sohu.com5分钟接口", results));
        content.append(genHtmlContent("plus.sohuno.com5分钟appkey", results2));
        content.append(genHtmlContent("passport.sohu.com5分钟接口", result3));
        content.append(genHtmlContent("plus.sohu.com5分钟接口", result4));
        content.append(HTML_END);
        mailService.sendMailToGroup("passport", content.toString());
        lastNotifyTime = System.currentTimeMillis();

    }

    public String genHtmlContent(String title, List<PassportEsResult> results) {
        StringBuilder content = new StringBuilder();
        content.append(String.format(TABLE_TITLE, title));
        for (PassportEsResult result : results) {
            content.append(String.format(TABLE, result.getColor(),result.getTimeKey(),
                    result.getInterfaceUri(),result.getCount(),result.getTotalCount(),
                    result.getLastCount(), result.getLastTotalCount()));
        }
        return content.toString();
    }

    void appendMonitorMsg(StringBuilder sb, List<PassportEsResult> results) {
        for (PassportEsResult result : results) {
            sb.append( String.format( "%s, %s, %s, %s, %s, %s\n",
                    result.getTimeKey(),result.getInterfaceUri(),result.getCount(),
                    result.getLastCount(),result.getLastCount(),result.getLastTotalCount() ) );
        }
    }
    public final String TABLE_TITLE = "<tr><td colspan=6>%s</td></tr>";
    public final String TABLE = "<tr style=\"background-color: %s\"> <td>%s</td> <td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>";
    public final String HTML_HEAD = "<!DOCTYPE html>" +
            "<html><head><title>PASSPORT监控</title>" +
            "<style type=\"text/css\">" +
            "body {" +
            "padding: 50px;" +
            "font: 14px \"Lucida Grande\", Helvetica, Arial, sans-serif;" +
            "}" +
            "a {" +
            "color: #00B7FF;" +
            "}" +
            "table {" +
            "border-right:1px solid #1c4587;" +
            "border-bottom:1px solid #1c4587;" +
            "}" +
            "table td {" +
            "border-left:1px solid #1c4587;" +
            "border-top:1px solid #1c4587;" +
            "}" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<table>" +
            "<tr><td>时间</td><td>接口</td><td>数量</td><td>总数</td><td>昨天的数量</td><td>昨天的总数</td></tr>";
    public final String HTML_END = "</table></body></html>";

}
