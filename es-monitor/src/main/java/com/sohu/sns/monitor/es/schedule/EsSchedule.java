package com.sohu.sns.monitor.es.schedule;

import com.sohu.sns.monitor.common.services.NotifyService;
import com.sohu.sns.monitor.es.module.EsResult;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.aggregations.metrics.avg.AvgBuilder;
import org.elasticsearch.search.aggregations.metrics.avg.InternalAvg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * author:jy
 * time:17-1-18下午8:29
 */
@Component
public class EsSchedule {
    @Autowired
    TransportClient client;
    @Autowired
    NotifyService notifyService;

    public void monitor() {
        Date endTime = new Date();
        Date monitorTime = new Date(endTime.getTime() - 5 * 60 * 1000);
        Date referenceTime = new Date(endTime.getTime() - 7 * 24 * 60 * 60 * 1000L);


        Map<String, EsResult> refResults = queryEs(referenceTime, endTime);
        Map<String, EsResult> monitorResults = queryEs(monitorTime, endTime);

        List<EsResult> notifyResults = new ArrayList<EsResult>();
        for (Map.Entry<String, EsResult> entry : monitorResults.entrySet()) {
            String key = entry.getKey();
            EsResult monitorEsResult = entry.getValue();
            long monitorTotoalCount = monitorEsResult.getTotoalCount();
            double monitroAvgTime = monitorEsResult.getAvgTime();

            if (monitorTotoalCount < 10 || monitroAvgTime < 0.2) {
                System.out.println(monitorEsResult + " is ok......");
                continue;
            }

            EsResult refResult = monitorResults.get(key);
            double refAvgTime = refResult.getAvgTime();
            long refTotoalCount = refResult.getTotoalCount();

            if (monitroAvgTime >= refAvgTime * 1.5 || monitorTotoalCount >= refTotoalCount * 1.5) {
                notifyResults.add(monitorEsResult);
                System.out.println(monitorEsResult + " is very very high......");
            }
        }

        StringBuilder sb = new StringBuilder().append("QPS预警");
        if (!notifyResults.isEmpty()) {
            for (EsResult result : notifyResults) {
                String key = result.getInterfaceUri();
                double qps = result.getQps();
                double avgTime = result.getAvgTime();
                if (qps > 1) {
                    sb.append(key + " qps:" + qps + " avg:" + avgTime + " | ");
                }
            }
            notifyService.sendAllNotifyPerson(sb.toString());
        }

    }

    public void monitorQps() {
        Date endTime = new Date();
        Date monitorTime = new Date(endTime.getTime() - 60 * 60 * 1000);
        Map<String, EsResult> monitorResults = queryEs(monitorTime, endTime);
        StringBuilder sb = new StringBuilder().append("QPS统计");
        for (Map.Entry<String, EsResult> entry : monitorResults.entrySet()) {
            String key = entry.getKey();
            EsResult result = entry.getValue();
            double qps = result.getQps();
            double avgTime = result.getAvgTime();
            if (qps > 1) {
                sb.append(key + " qps:" + qps + " avg:" + avgTime + " | ");
            }
        }
        notifyService.sendAllNotifyPerson(sb.toString());
    }

    private Map<String, EsResult> queryEs(Date startTime, Date endTime) {
        TermsBuilder interfaceCount = AggregationBuilders.terms("interfaceCount")
                .field("interface.raw")
                .size(100)
                .order(Terms.Order.count(true));
        AvgBuilder interfaceAvgTime = AggregationBuilders.avg("interfaceAvgTime").field("request_time");
        QueryBuilder qb = QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("@timestamp").gte(startTime).lte(endTime))
                .must(QueryBuilders.regexpQuery("interface.raw", "/v[56]/.*"));
        SearchResponse searchResponse = client.prepareSearch("logstash-detail-msapi*")
                .setQuery(qb)
                .addAggregation(interfaceCount.subAggregation(interfaceAvgTime))
                .setExplain(true)
                .execute()
                .actionGet();

        StringTerms interfaceCountAggr = (StringTerms) searchResponse.getAggregations().asMap().get("interfaceCount");
        List<Terms.Bucket> buckets = interfaceCountAggr.getBuckets();
        Map<String, EsResult> results = new HashMap();
        for (Terms.Bucket bucket : buckets) {
            long count = bucket.getDocCount();
            String uri = bucket.getKey().toString();
            InternalAvg aggregation = (InternalAvg) bucket.getAggregations().asList().get(0);
            double avg = aggregation.getValue();
            double qps = count / ((endTime.getTime() - startTime.getTime()) / 1000);

            EsResult esResult = new EsResult();
            esResult.setAvgTime(avg);
            esResult.setQps(qps);
            esResult.setInterfaceUri(uri);
            esResult.setTotoalCount(count);
            results.put(uri, esResult);
        }

        return results;
    }
}
