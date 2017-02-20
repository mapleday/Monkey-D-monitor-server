package com.sohu.sns.monitor.es.schedule;

import com.sohu.sns.monitor.common.module.EsResult;
import com.sohu.sns.monitor.common.services.NotifyService;
import com.sohu.sns.monitor.common.services.QpsDetailService;
import com.sohu.sns.monitor.es.module.PassportEsResult;
import com.sohu.sns.monitor.es.query.PassportEsAnalysis;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.avg.AvgBuilder;
import org.elasticsearch.search.aggregations.metrics.avg.InternalAvg;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
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

    public void monitor() {
        //查询今天和昨天的最近5分钟接口调用情况
        List<PassportEsResult> results = PassportEsAnalysis.getInstance().analysisTowDayQpm(1.6f);
        List<PassportEsResult> results2 = PassportEsAnalysis.getInstance().analysisTowDayAppKey(2.6f);

        StringBuilder sb = new StringBuilder();
        if (!results.isEmpty()) {
            sb.append("internal.passport.sohu.com5分钟接口\n");
            appendMonitorMsg(sb, results);
        }
        if (!results2.isEmpty()) {
            sb.append("plus.sohuno.com5分钟appkey\n");
            appendMonitorMsg(sb, results2);
        }
        System.out.println(sb.toString());
        notifyService.sendNotifyToPersonGroup(sb.toString(),"passport");
        lastNotifyTime = System.currentTimeMillis();

    }

    void appendMonitorMsg(StringBuilder sb, List<PassportEsResult> results) {
        for (PassportEsResult result : results) {
            sb.append( String.format( "%s,%s,数量=%s,昨天=%s,总计=%s,昨天总计=%s\n",
                    result.getTimeKey(),result.getInterfaceUri(),result.getCount(),
                    result.getLastCount(),result.getLastCount(),result.getLastTotalCount() ) );
        }
    }

}
