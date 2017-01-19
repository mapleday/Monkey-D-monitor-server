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

import java.text.DecimalFormat;
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
        // 5分钟和最近一周时间。
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

            EsResult refResult = refResults.get(key);
            double refAvgTime = refResult.getAvgTime();
            long refTotoalCount = refResult.getTotoalCount();



            if (monitroAvgTime >= refAvgTime * 1.3 || monitorTotoalCount >= refTotoalCount * 1.3) {
                notifyResults.add(monitorEsResult);
                System.out.println(monitorEsResult + " is very very high......");
            }else if(monitorEsResult.getQps()>=50){
                notifyResults.add(monitorEsResult);
                System.out.println(monitorEsResult+":QPS is very very high......");
            }

        }

        Set<EsResult> orderResults = new TreeSet();
        if (!notifyResults.isEmpty()) {
            for (EsResult result : notifyResults) {
                double qps = result.getQps();
                if (qps > 1) {
                    orderResults.add(result);
                }
            }
        }

        StringBuilder sb = new StringBuilder().append("QPS预警");
        if (!orderResults.isEmpty()) {
            for (EsResult result : orderResults) {
                String key = result.getInterfaceUri();
                double qps = result.getQps();
                double avgTime = result.getAvgTime();
                sb.append(key + " qps:" + qps + " avg:" + avgTime + " | ");
            }
            notifyService.sendAllNotifyPerson(sb.toString());
        }

    }


    public void monitorQps() {
        Date endTime = new Date();
        Date monitorTime = new Date(endTime.getTime() - 60 * 60 * 1000);
        Map<String, EsResult> monitorResults = queryEs(monitorTime, endTime);

        StringBuilder sb = new StringBuilder().append("QPS统计");
        Set<String> speInterfaceUri = new HashSet<String>();
        speInterfaceUri.add("/v6/users/show_reduced");
        speInterfaceUri.add("/v6/users/guide");
        speInterfaceUri.add("/v6/feeds/profile/template");
        speInterfaceUri.add("/v6/feeds/timeline/template");


        Set<EsResult> orderResults = new TreeSet();
        //总qps 统计进去。
        EsResult sumResult=new EsResult();
        for (Map.Entry<String, EsResult> entry : monitorResults.entrySet()) {

            EsResult result = entry.getValue();
            sumResult.setQps(sumResult.getQps()+entry.getValue().getQps());

            //特定接口QPS
            String key = result.getInterfaceUri();
            if(speInterfaceUri.contains(key)) {
                double qps = result.getQps();
                double avgTime = result.getAvgTime();
                sb.append(key + " qps:" + qps + " avg:" + avgTime + " \n ");
            }

            double qps = result.getQps();
            if (qps > 1) {
                orderResults.add(result);
            }
        }
        orderResults.add(sumResult);
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
            double qps = count / ((endTime.getTime() - startTime.getTime()) / 1000.0);

            EsResult esResult = new EsResult();
            DecimalFormat decimalFormat = new DecimalFormat("0.000");
            esResult.setAvgTime(Double.parseDouble(decimalFormat.format(avg)));
            esResult.setQps(Double.parseDouble(decimalFormat.format(qps)));
            esResult.setInterfaceUri(uri);
            esResult.setTotoalCount(count);
            results.put(uri, esResult);
        }

        return results;
    }

    public static void main(String[] args) {
        DecimalFormat decimalFormat = new DecimalFormat("0.000");
        System.out.println(decimalFormat.format(1.036));
    }
}
