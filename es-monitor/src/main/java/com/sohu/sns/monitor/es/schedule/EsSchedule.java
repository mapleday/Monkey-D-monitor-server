package com.sohu.sns.monitor.es.schedule;

import com.sohu.sns.monitor.common.module.EsResult;
import com.sohu.sns.monitor.common.services.NotifyService;
import com.sohu.sns.monitor.common.services.QpsDetailService;
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

import java.text.SimpleDateFormat;
import java.util.Date;
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
    private static long lastNotifyTime = 0;//上次预警时间

    @Autowired
    TransportClient client;
    @Autowired
    QpsDetailService qpsDetailService;

    @Autowired
    NotifyService notifyService;

    public void monitor() {
        Date currentTime = new Date();
        // 5分钟和最近一周时间。
        Date monitorTime = new Date(currentTime.getTime() - 5 * 60 * 1000);
        Date referenceStartTime = new Date(currentTime.getTime() - 7 * 24 * 60 * 60 * 1000L);
        Date referenceEndTime = new Date(currentTime.getTime() - 10 * 60 * 1000L);


        Map<String, EsResult> refResults = queryEs(referenceStartTime, referenceEndTime);
        Map<String, EsResult> monitorResults = queryEs(monitorTime, currentTime);

        Set<EsResult> orderResults = new TreeSet();
        for (Map.Entry<String, EsResult> entry : monitorResults.entrySet()) {
            String key = entry.getKey();
            EsResult monitorEsResult = entry.getValue();
            long monitorTotoalCount = monitorEsResult.getTotoalCount();
            double monitroAvgTime = monitorEsResult.getAvgTime();

            EsResult refResult = refResults.get(key);
            if (refResult == null) {
                System.out.println(monitorEsResult + "is error......");
                continue;
            }
            double refAvgTime = refResult.getAvgTime();
            long refTotoalCount = refResult.getTotoalCount();


            boolean isHighResult = monitroAvgTime >= refAvgTime * 1.3
                    || monitorTotoalCount >= refTotoalCount * 1.3
                    || monitorEsResult.getAvgTime() >= 0.5;
            boolean isCanNotify = (System.currentTimeMillis() - lastNotifyTime > 30 * 60 * 1000)
                    || monitorEsResult.getAvgTime() >= 1.5;
            boolean notNotify = monitorEsResult.getQps() < 1 || monitorEsResult.getAvgTime() < 0.2;
            if (isHighResult && isCanNotify && !notNotify) {
                orderResults.add(monitorEsResult);
                System.out.println(monitorEsResult + " is very very high......");
            }

        }

        StringBuilder sb = new StringBuilder().append("QPS预警\n");
        if (!orderResults.isEmpty()) {
            for (EsResult result : orderResults) {
                String key = result.getInterfaceUri();
                double qps = result.getQps();
                double avgTime = result.getAvgTime();
                sb.append(key + " qps:" + qps + " avg:" + avgTime + " \n ");
            }

            System.out.println(sb.toString());
            notifyService.sendAllNotifyPerson(sb.toString());
            lastNotifyTime = currentTime.getTime();
        }

    }


    public void monitorQps() {
        Date endTime = new Date();
        Date monitorTime = new Date(endTime.getTime() - 60 * 60 * 1000);
        Set<String> speInterfaceUri = new HashSet<String>();
        speInterfaceUri.add("/v6/users/show_reduced");
        speInterfaceUri.add("/v6/users/guide");
        speInterfaceUri.add("/v6/feeds/profile/template");
        speInterfaceUri.add("/v6/feeds/timeline/template");
        Set<EsResult> orderResults = new TreeSet();
        StringBuilder sb = new StringBuilder().append("QPS统计 \n");

        Map<String, EsResult> monitorResults = queryEs(monitorTime, endTime);
        Map<String,EsResult> averageMonitorResults = queryEs(new Date(endTime.getTime() - 7*60*60*1000),new Date(endTime.getTime() - 60*60*1000));



        System.out.println(monitorResults);
        //总qps 统计进去。
        EsResult sumResult = new EsResult();
        EsResult averageResult = new EsResult();

        //qps峰值
        InternalHistogram.Bucket bucket = queryQpsMax(monitorTime, endTime);
        SimpleDateFormat dateformat=new SimpleDateFormat("HH:mm:ss");
        DateTime maxKey = (DateTime) bucket.getKey();
        sb.append("峰值:   "+bucket.getDocCount()+"    "+dateformat.format(new Date(maxKey.getMillis())).toString()+"\n");


        sumResult.setInterfaceUri("总计：");
        for (Map.Entry<String, EsResult> entry : monitorResults.entrySet()) {
            EsResult result = entry.getValue();
            double qps = result.getQps();
            sumResult.setQps(sumResult.getQps() + result.getQps());
            sumResult.setAvgTime(sumResult.getAvgTime()+result.getAvgTime());
            sumResult.setTotoalCount(sumResult.getTotoalCount() + result.getTotoalCount());

            //特定接口QPS
            String key = result.getInterfaceUri();
            if (speInterfaceUri.contains(key) || qps > 1) {
                orderResults.add(result);
            }
        }

        //前6小时平均qps
        for (Map.Entry<String,EsResult> entry:averageMonitorResults.entrySet()){
            EsResult result = entry.getValue();
            averageResult.setQps(averageResult.getQps()+result.getQps());
            averageResult.setAvgTime(averageResult.getAvgTime()+result.getAvgTime());
            averageResult.setTotoalCount(averageResult.getTotoalCount()+result.getTotoalCount());
        }


        orderResults.add(sumResult);


        DecimalFormat decimalFormat = new DecimalFormat("0.000");

        for (EsResult result : orderResults) {

            //定期更新
            if(result.getInterfaceUri()!=null) {
                qpsDetailService.updateDetail(result);
            }

            String key = result.getInterfaceUri();

            double qps = Double.parseDouble(decimalFormat.format(result.getQps()));
            double avgTime = result.getAvgTime();

            sb.append(key + " qps:" + qps + " avg:" + avgTime + " total：" + result.getTotoalCount() + " \n ");


        }

        System.out.println(sb.toString());

        if(sumResult.getQps()>averageResult.getQps()*1.3 || sumResult.getQps()< averageResult.getQps()/1.3 || sumResult.getAvgTime()>averageResult.getAvgTime()*1.3)  {
            notifyService.sendAllNotifyPerson(sb.toString());
        }

    }

    private Map<String, EsResult> queryEs(Date startTime, Date endTime) {

        TermsBuilder interfaceCount = AggregationBuilders.terms("interfaceCount")
                .field("interface.raw")
                .size(300)
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

    private InternalHistogram.Bucket queryQpsMax(Date startTime, Date endTime) {

        // 查询QPS峰值
        QueryBuilder qb1 = QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("@timestamp").gte(startTime).lte(endTime))
                .must(QueryBuilders.regexpQuery("interface.raw", "/v[56]/.*"))
                .mustNot(QueryBuilders.regexpQuery("interface.raw","/v6/topic/feed/repost_count")) ;

        DateHistogramBuilder qpsDate = AggregationBuilders.dateHistogram("timestamp").field("@timestamp").order(Histogram.Order.COUNT_DESC)
                .interval(DateHistogramInterval.seconds(1)).minDocCount(1).timeZone("Asia/Shanghai");

        SearchResponse searchResponse1 = client.prepareSearch("logstash-detail*")
                .setQuery(qb1)
                .setSize(1)
                .addAggregation(qpsDate)
                .execute()
                .actionGet();

        InternalHistogram internalHistogram = (InternalHistogram) searchResponse1.getAggregations().asList().get(0);

        InternalHistogram.Bucket bucket = (InternalHistogram.Bucket)internalHistogram.getBuckets().get(0);

        return bucket;

    }


}
