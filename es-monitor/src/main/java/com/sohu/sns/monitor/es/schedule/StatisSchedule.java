package com.sohu.sns.monitor.es.schedule;

import com.sohu.sns.monitor.common.services.NotifyService;
import com.sohu.sns.monitor.es.module.Message;
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
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by lc on 17-2-27.
 */

@Component
public class StatisSchedule {

    @Autowired
    TransportClient client;
    @Autowired
    NotifyService notifyService;


    public void statisMessageCount() {
        Date startTime = new Date(System.currentTimeMillis()-24*60*60*1000);
        Date endTime = new Date();
        StringBuilder sb = new StringBuilder();

        try {

            SearchResponse typeResponse = classificationStatis(startTime,endTime,"type");
            StringTerms docCountAggr = (StringTerms) typeResponse.getAggregations().asMap().get("count");
            List<Terms.Bucket> buckets = docCountAggr.getBuckets();
            Set<Message> messageSet = new TreeSet<Message>();

            for (Terms.Bucket bucket : buckets) {
                Message message = new Message();
                String messagename = bucket.getKey().toString();
                message.setMessageName(messagename);
                message.setCount(bucket.getDocCount());
                InternalAvg aggregation = (InternalAvg) bucket.getAggregations().asList().get(0);
                double avg = aggregation.getValue();
                message.setAvgTime(avg);
                messageSet.add(message);
            }
            Message message = new Message();
            message.setCount(typeResponse.getHits().getTotalHits());
            message.setMessageName("消息总量:");
            messageSet.add(message);

            DecimalFormat decimalFormat = new DecimalFormat("0.00");

            if (!messageSet.isEmpty()){
                for (Message msg:messageSet){
                    String messagename = msg.getMessageName();
                    int count = Integer.parseInt(msg.getCount()+"");
                    double avgTime = Double.parseDouble(decimalFormat.format(msg.getAvgTime()));
                    sb.append(messagename+" 消息量:"+count+"  平均响应时间:"+avgTime+"\n");
                }
            }



            SearchResponse exceptionResponse = classificationStatis(startTime,endTime,"exceptionstate");
            StringTerms exceptionDocCountAggr = (StringTerms) exceptionResponse.getAggregations().asMap().get("count");
            List<Terms.Bucket> exceptionBuckets = exceptionDocCountAggr.getBuckets();
            if (exceptionBuckets.size()!=0){
                for (Terms.Bucket bucket : exceptionBuckets) {
                    sb.append("异常:"+bucket.getKey());
                    sb.append(bucket.getDocCount()+"\n");
                }
            }else{
                sb.append("异常数量:0 \n");
            }


            SearchResponse normalResponse = classificationStatis(startTime,endTime,"returnstate");
            StringTerms normalDocCountAggr = (StringTerms) normalResponse.getAggregations().asMap().get("count");
            List<Terms.Bucket> normalBuckets = normalDocCountAggr.getBuckets();
            for (Terms.Bucket bucket : normalBuckets) {
                if(bucket.getKey().equals("1")) {
                    sb.append("正常数量:"+bucket.getDocCount()+"\n");
                }
            }

            notifyService.sendEmailAllNotifyPerson(sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    private SearchResponse classificationStatis(Date startTime, Date endTime, String classification) throws Exception{

        TermsBuilder messageCount = AggregationBuilders.terms("count")
                .field(classification)
                .size(100)
                .order(Terms.Order.count(true));

        AvgBuilder invoketime = AggregationBuilders.avg("invoketime").field("invoketime");

        QueryBuilder qb = QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("@timestamp").gte(startTime).lte(endTime));

        SearchResponse searchResponse = client.prepareSearch("apimqservice_statis_test-*")  //index
                .setQuery(qb)
                .addAggregation(messageCount.subAggregation(invoketime))
                .setExplain(true)
                .execute()
                .actionGet();

        return searchResponse;
    }
}
